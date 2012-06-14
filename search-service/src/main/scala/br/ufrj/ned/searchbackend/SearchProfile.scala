package br.ufrj.ned.searchbackend

import scala.collection.JavaConversions._

import com.codahale.logula.Logging
import com.hp.hpl.jena.query._
import collection.mutable.ArrayBuffer
import br.ufrj.ned.tools.XSDValidator
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
                    val popularity : Seq[PopularityMethod],
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
    //TODO: SUpport for multiple popularity scores
    var results = weightedResults
    for(popMethod <- popularity) {
        val popMeasurer = new PopularityMeasurer(queryUrl, popMethod)
        val entities = results map {_.uri}
        val popularities = popMeasurer.getPopularities(entities)

        results = for(i <- 0 to results.length-1) yield
          new SearchResult(results(i), popularities(i))
    }
    results
  }

  /** Takes a ResultSet and creates a Seq of SearchResults weighted with 
   * their match score.
   * 
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
    for(uri <- scoredResults.keysIterator ; if(URI.isValid(uri)))
      resultArray += new SearchResult(new URI(uri), scoredResults(uri).toFloat)

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
    <profile>
      <name>{name}</name>
      <end-point>
        <url>{queryUrl}</url>
      </end-point>
      <search>
        {predicates.values.map(pred => pred.toXML)}
      </search>
      <popularity>
        {popularity.map(pop => pop.toXML)}
      </popularity>
      <type-constraint>
        {types.map(typeUri => <type>{typeUri}</type>)}
      </type-constraint>
    </profile>
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

      /** Checks wether the XML is valid according to the provided schema */
      val schemaIn = this.getClass.getResourceAsStream("profile.xsd")
      if(schemaIn == null)
        log.error("Unable to find XML schema for profiles")
      else if(!XSDValidator.validate(config.mkString, schemaIn))
        throw new InvalidProfileException("The XML profile doesn't match the XSD")

      val name = (config\"name").text
      val queryUrl = (config\"end-point"\"url").text
      
      /* Get the predicates to use */
      var predicates = new HashMap[String, SearchPredicate]()

      for (predNode <- config\"search"\"search-predicate") {
        val predicate = SearchPredicate(predNode)
        predicates += (predicate.key -> predicate)
      }

      /* Get the popularity measurement method */
      val popMeasures = config\"popularity"\"measure"
      val popMethods = 
        for(measureNode <- popMeasures) yield
          PopularityMethod(measureNode)

      /* get the type constraints */
      val typeConstraints = config\"type-constraint"\"type"
      var constraints = new ListSet[URI]
      for(typeUri <- typeConstraints.map(_.text) ; if URI.isValid(typeUri))
        constraints += new URI(typeUri)
      
      Some(new SearchProfile(name,
                             queryUrl,
                             predicates,
                             popMethods,
                             constraints))
    } catch {
      case e => 
        log.error("Unable to read XML : %s", e)
        None
    }
  }

}