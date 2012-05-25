package br.ufrj.ned.searchbackend

/* Allow using for on java iterators */
import scala.collection.JavaConversions._

import com.codahale.logula.Logging
import com.hp.hpl.jena.query._
import collection.mutable.ArrayBuffer
import collection.immutable.HashMap
import scala.collection.immutable.ListSet


/** This class aims at storing information about a search backend
  * (ex : a DBPeida SPARQL end point)
  * 
  * It provides ways to create queries against this backend
  */
class SearchBackend(val name : String,
                    val queryUrl : String,
                    val predicates : HashMap[String, Predicate],
                    val matchInfo : Match,
                    val popularity : Option[PopularityMethod],
                    val types : Set[String]
                     ) extends Logging {

  def search(searchTerm : String) : Seq[SearchResult] = {
    log.info("Searching for \"%s\" on <%s>", searchTerm, queryUrl)
    
    val query = SearchQueryFactory.create(searchTerm, this)
    
    log.info("Remote query execution start")
    val rawResults = QueryExecutionFactory.sparqlService(queryUrl, query).execSelect()
    log.info("Remote query execution end")

    val weightedResults = treatResults(rawResults)
    val results = retrievePopularityScore(weightedResults)

    return util.Sorting.stableSort(results)
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
        val uri = SearchBackend.normalizeUri(sol.getResource(varName).toString)
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
    val it = predicates.valuesIterator
    while(it.hasNext) {
      val p = it.next
      ret += p.uri + " - " + p.weight + "\n"
    }
    
    return ret
  }

}

/**Companion object for SearchBackend defining a constructor to
 * parse an XML config file.
 *
 * Usage : `val sb = SearchBackend(scala.xml.XML.loadFile(configFile))`
 */
object SearchBackend extends Logging {

  /**
   * Constructor for SearchBackend
   *
   * @param configFile a file path containing the XML configuration
   * @return a SearchBackend built from the configuration
   */
  def apply(configFile : String) : Option[SearchBackend] =
    SearchBackend(scala.xml.XML.loadFile(configFile))

  /**Constructor for SearchBackend
   *
   * @param config an xml node containing the configuration
   * @return a SearchBackend built from the configuration
   */
  def apply(config : scala.xml.Node) : Option[SearchBackend] = {
    try {
      val name = (config\"name").text
      val queryUrl = (config\"end-point"\"url").text
      
      /* Get the predicates to use */
      var predicates = new collection.immutable.HashMap[String, Predicate]()
      for (predNode <- config\"search"\"search-predicate") {
        val uri = normalizeUri((predNode\"@uri").text)
        val pred = Predicate(uri, (predNode\"@weight").text.toFloat)
        predicates += (pred.key -> pred)
      }
      
      /* Get match method to use */
      val matchMethod = (config\"search"\"match"\"type").text
      val containsUri = 
        if(matchMethod == "contains")
          normalizeUri((config\"search"\"match"\"contains-uri").text)
      else
        ""
      
      /* Get the popularity measurement method */
      val popMeasure = config\"popularity"\"measure"
      val popMethod = 
        if(popMeasure.isEmpty) {
          None
        } else {
          val popPredicate = normalizeUri((popMeasure\"predicate").text)
          Some(new PopularityMethod(popPredicate))
        }

      /* get the type constraints */
      val typeConstraints = config\"type-constraint"\"type"
      var constraints = new ListSet[String]
      for(typeUri <- typeConstraints)
        constraints += normalizeUri(typeUri.text)
      
      Some(new SearchBackend(name,
                             queryUrl,
                             predicates,
                             new Match(matchMethod, containsUri),
                             popMethod,
                             constraints))
    } catch {
      case e => 
        log.error("Unable to read XML : %s", e)
        None
    }
  }

  /** 
   * Normalize a URI for use in SPARQL.
   * 
   * @param uriText URI to normalize
   * @return <uriText> if the URI doesn't use a prefix, uriText otherwise
   */
  def normalizeUri(uriText : String) : String = {
    def clean(uri:String) = 
      if(uri.contains("\"")) {
        log.error("This URI contains double quotes : "+uri)
        uri.replaceAll("\"", "")
      } else
        uri

    if(uriText.startsWith("http://") || uriText.startsWith("bif:"))
      "<"+clean(uriText)+">"
    else
      clean(uriText)
  }
}