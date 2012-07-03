package br.ufrj.ned.searchbackend

import com.codahale.logula.Logging
import scala.collection.immutable.HashMap

/** 
 * This class stores the uri of a predicate
 * and the its associated weight (i.e. how important
 * is this predicate to know which candidate matches the best)
 */
abstract class SearchPredicate(val uri : URI, val weight : Float) extends Logging {

  /**
   * This key represent the variable name to store partial results in the SPARQL
   * query. Therefore to different SearchPredicate (by URI or method) should have 
   * different keys.
   */
  val key:String

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

  def this(uri:String, weight:Float, language:Option[String]) =
    this(new URI(uri), weight, language)


  def this(uri:String, weight:Float) = this(uri, weight, None)

  def this(uri:String, weight:Float, language:String) = this(uri, weight, Some(language))

  val key = uri.xml.filter(SearchPredicate.allowedKeyChars.contains(_)).mkString

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

  def this(uri:String, weight:Float, containsUri:String) =
    this(new URI(uri), weight, new URI(containsUri))

  val key = uri.xml.filter(SearchPredicate.allowedKeyChars.contains(_)).mkString +
    containsUri.xml.filter(SearchPredicate.allowedKeyChars.contains(_)).mkString 

  /**
   * Take the term that is searched and format in order to fit in a SPARQL query.
   * 
   * This is mainly intented to deal with corner cases such as bif:contains which
   * doesn't accept space in the search string.
   */
  def formatSearchTerm(searchTerm:String) = {
    if(containsUri.xml == "bif:contains") {
      val parts = searchTerm.split(" ").map("'"+_+"'")
      "\""+parts.reduceLeft(_+" AND "+_) + "\""
    } else {
      "\""+searchTerm+"\""
    }
  }

  override def toSPARQL(searchTerm : String) = {
    "?" + key + " " + uri.sparql + " ?containsText ." +
    "?containsText " +containsUri.sparql+" "+formatSearchTerm(searchTerm)+ " .  "
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
    
    val uri = (predNode\"uri").text
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
          val containsUri = (predNode\"contains-uri").text
          if(containsUri.isEmpty)
            throw new Exception("No full text search predicate was provided")
          new ContainsPredicate(uri, weight, containsUri)
        case _ => 
          throw new Exception("Unable to recognize the match method : " +method)
      }
    
    pred
  }
}