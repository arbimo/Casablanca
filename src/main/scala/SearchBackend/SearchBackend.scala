package br.ufrj.ner.SearchBackend


import com.codahale.logula.Logging
import com.hp.hpl.jena.query._
import scala.collection.mutable.ArrayBuffer


/** This class stores the uri of a predicate 
  * and the its associated weight (i.e. how important
  * is this predicate to know which candidate matches the best)
  */
case class Predicate(uri : String, weight : Int) {
  private final val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  val key = { 
    uri.foldLeft("?")( (key, currentChar) => if(chars.contains(currentChar)) key + currentChar 
                                          else key ) 
  }
}

/** This class contains a single result from a search.
  * 
  * The result is described with its URI and its score
  */
case class SearchResult(uri : String, score : Float)


/** This class aims at storing information about a search backend
  * (ex : a DBPeida SPARQL end point)
  * 
  * It provides ways to create queries against this backend
  */
class SearchBackend extends Logging {

  var name = ""
  
  var url = ""
  
  var predicates = new ArrayBuffer[Predicate](0)
  
  def search(searchTerm : String) : ResultSet = {
    log.info("Searching for \"%s\" on <%s>", searchTerm, url)
    
    val query = SearchQueryFactory.create(searchTerm, this)
    
    log.info("Remote query execution start")
    val results = QueryExecutionFactory.sparqlService(url, query).execSelect()
    log.info("Remote query execution end")
    
    
    return results
  }
  
  private def sortResults(results : ResultSet) : ArrayBuffer[SearchResult] = {
  
    return new ArrayBuffer[SearchResult](0)
  }
  
  override def toString : String = { 
    var ret = "Name : " + name + "\n" 
    ret += "Url : " + url + "\n"
    
    ret +=  "\nPredicates list with their weight : \n"
    for(p <- predicates) 
      ret += p.uri + " - " + p.weight + "\n"
    
    return ret
  }

}