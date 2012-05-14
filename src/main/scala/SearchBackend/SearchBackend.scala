package br.ufrj.ner.SearchBackend

/* Allow using for on java iterators */
import scala.collection.JavaConversions._

import com.codahale.logula.Logging
import com.hp.hpl.jena.query._
import collection.mutable.{HashMap, ArrayBuffer}


/** This class stores the uri of a predicate
  * and the its associated weight (i.e. how important
  * is this predicate to know which candidate matches the best)
  */
case class Predicate(uri : String, weight : Int) {
  private final val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  val key = { 
    uri.foldLeft("")( (key, currentChar) => if(chars.contains(currentChar)) key + currentChar
                                          else key ) 
  }
}

object NullPredicate extends Predicate("", 0)

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
class SearchBackend extends Logging {

  var name = ""
  
  var url = ""
  
  var predicates = new scala.collection.mutable.HashMap[String, Predicate]()
  
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
    val scoredResults =  new HashMap[String, Int]()

    for(sol <- results ; varName <- sol.varNames()) {

      /* if varName match to one of our predicates */
      if(predicates.contains(varName)) {
        val uri = sol.getResource(varName).toString
        val pred = predicates(varName)
        val oldScore = scoredResults.getOrElse(uri, 0)
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