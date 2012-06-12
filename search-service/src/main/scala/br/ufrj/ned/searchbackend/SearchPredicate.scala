package br.ufrj.ned.searchbackend

import com.codahale.logula.Logging
import scala.collection.immutable.HashMap

/** 
 * This class stores the uri of a predicate
 * and the its associated weight (i.e. how important
 * is this predicate to know which candidate matches the best)
 */
abstract class SearchPredicate(val uri : URI, val weight : Float) extends Logging {

  val key = uri.xml.filter(SearchPredicate.allowedKeyChars.contains(_)).mkString

  /**
   * An XML representation of the SearchPredicate
   */
  def toXML = 
    <search-predicate>
      <uri>{uri.xml}</uri>
      <weight>{weight.toString}</weight>
    </search-predicate>
  
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
class ExactMatchPredicate(uri:URI, weight:Float, language:Option[String]) 
            extends SearchPredicate(uri, weight) {

  def this(uri:URI, weight:Float) = this(uri, weight, None)

  def this(uri:URI, weight:Float, language:String) = this(uri, weight, Some(language))

  override def toSPARQL(searchTerm : String) = {
    val languageFilter = language match {
      case None => ""
      case Some(lg:String) => "@"+lg
    }

    "?" + key + " " + uri.sparql + " \"" + 
    searchTerm + "\"" + languageFilter + " .  "
  }

  override def toXML = 
    <search-predicate>
      <uri>{uri.xml}</uri>
      <weight>{weight.toString}</weight>
      <method>exact</method>
      {language match {
        case Some(lg:String) => <language>{lg}</language>
        case None => 
      }}
    </search-predicate>
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
class ContainsPredicate(uri:URI, weight:Float, containsUri:URI)
            extends SearchPredicate(uri, weight) {

  override def toSPARQL(searchTerm : String) = {
    "?" + key + " " + uri.sparql + " ?containsText ." +
    "?containsText " + containsUri.sparql + " \"" + searchTerm + "\" .  "
  }

  override def toXML = 
    <search-predicate>
      <uri>{uri.xml}</uri>
      <weight>{weight.toString}</weight>
      <method>contains</method>
      <contains-uri>{containsUri.xml}</contains-uri>
    </search-predicate>
}

object SearchPredicate {
  /**
   * A list of char alloweds in SPARQL variable names.
   */
  val allowedKeyChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  /**
   * Takes a <search-predicate> node of a configuration and returns the
   * corresponding SearchPredicate instance.
   */
  def apply(predNode : scala.xml.Node) : SearchPredicate = {
    
    /* Get the predicates to use */
    var predicates = new HashMap[String, SearchPredicate]()
    
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
    
    
    pred
  }
}