package br.ufrj.ned.searchbackend

import com.codahale.logula.Logging

/** 
 * This class stores the uri of a predicate
 * and the its associated weight (i.e. how important
 * is this predicate to know which candidate matches the best)
 */
abstract class SearchPredicate(val uri : String, val weight : Float) extends Logging {

  val key = uri.filter(SearchPredicate.allowedKeyChars.contains(_)).mkString

  /**
   * An XML representation of the SearchPredicate
   */
  def toXML = 
    <search-predicate uri={uri} weight={weight.toString}/>

  /**
   * A SPARQL representation of the search predicate that is meant to be used 
   * when looking for candidates.
   * 
   * @param searchTerm The search term corresponding to the query
   */
  def toSPARQL(searchTerm:String) : String
}

/**
 * This class correspond to predicate that are used to perform an exact match search.
 * 
 * The SPARQL schema is the following :
 * 
 * ?key <http://.../search-predicate-uri "search-term"
 */
class ExactMatchPredicate(uri:String, weight:Float) 
            extends SearchPredicate(uri, weight) {

  override def toSPARQL(searchTerm : String) = {
    "?" + key + " " + uri + " \"" + searchTerm + "\" .  "
  }

  override def toXML = 
    <search-predicate uri={uri} weight={weight.toString} match="exact"/>
}

/**
 * This class correspond to predicates that are used to perform a full-text search 
 * using a RDF store specific predicates (for example, Virtuoso's bif:contains)
 * 
 * The SPARQL schema is the following :
 * 
 * ?key <http://.../search-predicate-uri ?containsText
 * ?containsText <contains-uri> "search-term"
 */
class ContainsPredicate(uri:String, weight:Float, containsUri:String)
            extends SearchPredicate(uri, weight) {

  override def toSPARQL(searchTerm : String) = {
    "?" + key + " " + uri + " ?containsText ." +
    "?containsText " + containsUri + " \"" + searchTerm + "\" .  "
  }

  override def toXML = 
    <search-predicate uri={uri} weight={weight.toString} 
      match="contains" contain-uri={containsUri}/>
}

object SearchPredicate {
  val allowedKeyChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')
}