package br.ufrj.ned.searchbackend

import scala.collection.JavaConversions._

import com.codahale.logula.Logging
import com.hp.hpl.jena.query._
import collection.mutable.ArrayBuffer
import collection.immutable.HashMap
import scala.collection.immutable.ListSet
import br.ufrj.ned.exceptions._
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP


/** This class aims at storing information about a search backend
  * (ex : a DBPeida SPARQL end point)
  * 
  * It provides ways to create queries against this backend
  */
class SearchProfile(val name : String,
                    val queryUrl : String,
                    val predicates : HashMap[String, SearchPredicate],
                    val popularity : Option[PopularityMethod],
                    val types : Set[URI]
                     ) extends Logging {

  def search(searchTerm : String) : Seq[SearchResult] = {
    try {
      log.info("Searching for \"%s\" on <%s>", searchTerm, queryUrl)
      
      val query = SearchQueryFactory.create(searchTerm, this)
      
      log.info("Remote query execution start")
      val rawResults = QueryExecutionFactory.sparqlService(queryUrl, query).execSelect()
      log.info("Remote query execution end")
      
      val weightedResults = treatResults(rawResults)
      val results = retrievePopularityScore(weightedResults)
      
      util.Sorting.stableSort(results)
    } catch {
      case e:QueryExceptionHTTP =>
        throw new RemoteEndPointException(e.toString)
    }
  }

  /**
   * This function retrieves popularity score of a Seq of SearchResult
   * 
   * @param weightedResults a Seq of SearchResults without popularityScores
   * @return a Seq of SearchResult with their corresponding popularity scores
   */
  private def retrievePopularityScore(weightedResults : Seq[SearchResult]) : Seq[SearchResult] = {
    popularity match {
      case Some(popMethod) =>
        val popMeasurer = new PopularityMeasurer(queryUrl, popMethod)
        val entities = weightedResults map {_.uri}
        val popularities = popMeasurer.getPopularities(entities)

        for(i <- 0 to weightedResults.length-1) yield
          new SearchResult(weightedResults(i), popularities(i))
      case None =>
        weightedResults
    }
  }

  /** Takes a ResultSet and creates an array sorted according of the weight
   * of each predicates applied to an entity
   */
  private def treatResults(results : ResultSet) : Seq[SearchResult] = {
    val scoredResults =  new collection.mutable.HashMap[String, Float]()

    for(sol <- results ; varName <- sol.varNames()) {

      /* if varName match to one of our predicates */
      if(predicates.contains(varName)) {
        val uri = sol.getResource(varName).toString
        val pred = predicates(varName)
        val oldScore = scoredResults.getOrElse(uri, 0f)
        scoredResults.update(uri, oldScore + pred.weight)
      }
    }

    var resultArray = new ArrayBuffer[SearchResult](scoredResults.size)
    for(key <- scoredResults.keysIterator)
      resultArray += new SearchResult(key, scoredResults(key).toFloat)

    return resultArray
  }
  
  override def toString : String = { 
    var ret = "Name : " + name + "\n" 
    ret += "Url : " + queryUrl + "\n"
    
    ret +=  "\nPredicates list with their weight : \n"
    for(p <- predicates.values) 
      ret += p.toString
    

    return ret
  }
  
  lazy val toXML = {
    <search-backend>
      <name>{name}</name>
      <end-point>
        <url>{queryUrl}</url>
      </end-point>
      <search>
        {predicates.values.map(pred => pred.toXML)}
      </search>
      <popularity>
        {popularity.toList.map(pop => pop.toXML)}
      </popularity>
      <type-constraint>
        {types.map(typeUri => <type>{typeUri}</type>)}
      </type-constraint>
    </search-backend>
  }
  
  
}

/**Companion object for SearchProfile defining a constructor to
 * parse an XML config file.
 *
 * Usage : `val sb = SearchProfile(scala.xml.XML.loadFile(configFile))`
 */
object SearchProfile extends Logging {
  
  /**
   * Constructor for SearchProfile
   *
   * @param configFile a file path containing the XML configuration
   * @return a SearchProfile built from the configuration
   */
  def apply(configFile : String) : Option[SearchProfile] =
    SearchProfile(scala.xml.XML.loadFile(configFile))

  /**Constructor for SearchProfile
   *
   * @param config an xml node containing the configuration
   * @return a SearchProfile built from the configuration
   */
  def apply(config : scala.xml.Node) : Option[SearchProfile] = {
    try {
      val name = (config\"name").text
      val queryUrl = (config\"end-point"\"url").text
      
      /* Get the predicates to use */
      var predicates = new collection.immutable.HashMap[String, SearchPredicate]()

      for (predNode <- config\"search"\"search-predicate") {
        val uri = new URI((predNode\"uri").text)
        val weight = (predNode\"weight").text.toFloat

        val method = 
          if((predNode\"method").isEmpty)
            "exact"
          else
            (predNode\"method").text

        val pred =
          method match {
            case "exact" =>
              if((predNode\"language").isEmpty)
                new ExactMatchPredicate(uri, weight)
              else
                new ExactMatchPredicate(uri, weight, (predNode\"language").text)
            case "contains" => 
              val containsUri = new URI((predNode\"contains-uri").text)
              new ContainsPredicate(uri, weight, containsUri)
            case _ => 
              throw new Exception("Unable to recognize the match method : " +method)
          }
        
        
        predicates += (pred.key -> pred)
      }
      
      
      /* Get the popularity measurement method */
      val popMeasure = config\"popularity"\"measure"
      val popMethod = 
        if(popMeasure.isEmpty) {
          None
        } else {
          val popPredicate = new URI((popMeasure\"predicate").text)
          Some(new PopularityMethod(popPredicate))
        }

      /* get the type constraints */
      val typeConstraints = config\"type-constraint"\"type"
      var constraints = new ListSet[URI]
      for(typeUri <- typeConstraints)
        constraints += new URI(typeUri.text)
      
      Some(new SearchProfile(name,
                             queryUrl,
                             predicates,
                             popMethod,
                             constraints))
    } catch {
      case e => 
        log.error("Unable to read XML : %s", e)
        None
    }
  }

}