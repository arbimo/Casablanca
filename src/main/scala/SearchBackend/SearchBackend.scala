package br.ufrj.ner.searchbackend

/* Allow using for on java iterators */
import scala.collection.JavaConversions._

import com.codahale.logula.Logging
import com.hp.hpl.jena.query._
import collection.mutable.ArrayBuffer
import collection.immutable.HashMap


/** This class stores the uri of a predicate
  * and the its associated weight (i.e. how important
  * is this predicate to know which candidate matches the best)
  */
case class Predicate(uri : String, weight : Float) {
  private final val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  val key =
    uri.foldLeft("")( (key, currentChar) => if(chars.contains(currentChar)) key + currentChar else key )
}

/** This class contains a single result from a search.
  * 
  * The result is described with its URI and its score
  */
case class SearchResult(uri : String, score : Float) extends Ordered[SearchResult] {
  override def compare(other : SearchResult) = - this.score.compare(other.score)
  override def toString() = score + " - " + uri
}


/** This class aims at storing information about a search backend
  * (ex : a DBPeida SPARQL end point)
  * 
  * It provides ways to create queries against this backend
  */
class SearchBackend(val name : String,
                    val url : String,
                    val predicates : HashMap[String, Predicate]
                     ) extends Logging {

  def search(searchTerm : String) : Seq[SearchResult] = {
    log.info("Searching for \"%s\" on <%s>", searchTerm, url)
    
    val query = SearchQueryFactory.create(searchTerm, this)
    
    log.info("Remote query execution start")
    val results = QueryExecutionFactory.sparqlService(url, query).execSelect()
    log.info("Remote query execution end")

    return treatResults(results)
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
        val oldScore :Float = scoredResults.getOrElse(uri, 0f)
        scoredResults.update(uri, oldScore + pred.weight)
      }
    }

    var resultArray = new ArrayBuffer[SearchResult](scoredResults.size)
    for(key <- scoredResults.keysIterator)
      resultArray += new SearchResult(key, scoredResults(key).toFloat)

    return util.Sorting.stableSort(resultArray)
  }
  
  override def toString : String = { 
    var ret = "Name : " + name + "\n" 
    ret += "Url : " + url + "\n"
    
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

  /**Constructor for SearchBackend
   *
   * @param config an xml node containing the configuration
   * @return a SearchBackend built from the configuration
   */
  def apply(config : scala.xml.Node) : SearchBackend = {
    val name = (config\"@name").text
    val queryUrl = (config\"@url").text

    var predicates = new collection.immutable.HashMap[String, Predicate]()
    for (predNode <- config \ "search-predicate") {
      val uri = {
        val uriText = (predNode\"@uri").text
        if (uriText.startsWith("http://")) "<" + uriText + ">"
        else uriText
      }
      val pred = Predicate(uri, (predNode\"@weight").text.toFloat)
      println(predNode + "\n" + pred )
      predicates += (pred.key -> pred)
    }

    return new SearchBackend(name, queryUrl, predicates)
  }
}