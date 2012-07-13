package br.ufrj.ned.searchbackend

import com.codahale.logula.Logging
import scala.collection.immutable.HashMap

/** 
 * This class stores the uri of a predicate
 * and the its associated weight (i.e. how important
 * is this predicate to know which candidate matches the best)
 */
abstract class SearchPredicate(uri : String, val weight : Float) 
     extends Predicate(uri) with Logging {

  /**
   * An XML representation of the SearchPredicate
   */
  def toXML = 
    <search-predicate>
      <uri>{xmlUri}</uri>
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
class ExactMatchPredicate(uri:String, weight:Float, language:Option[String]) 
            extends SearchPredicate(uri, weight) {

  def this(uri:String, weight:Float) = this(uri, weight, None)

  def this(uri:String, weight:Float, language:String) = this(uri, weight, Some(language))


  override def toSPARQL(searchTerm : String) = {
    val languageFilter = language match {
      case None => ""
      case Some(lg:String) => "@"+lg
    }

    "?" + key + " " + sparqlUri + " \"" + 
    searchTerm + "\"" + languageFilter + " .  "
  }

  override def toXML = 
    <search-predicate>
      <uri>{xmlUri}</uri>
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
class ContainsPredicate(uri:String, weight:Float, containsUri:URI)
            extends SearchPredicate(uri, weight) {

  def this(pred:String, weight:Float, containsUri:String) =
    this(pred, weight, new URI(containsUri))
  
  override val key = defaultKey + Predicate.getKey(containsUri.xmlUri)

  /**
   * Take the term that is searched and format in order to fit in a SPARQL query.
   * 
   * This is mainly intented to deal with corner cases such as bif:contains which
   * doesn't accept space in the search string.
   */
  def formatSearchTerm(searchTerm:String) = {
    if(containsUri.xmlUri == "bif:contains") {
      val parts = searchTerm.split(" ").map("'"+_+"'")
      "\""+parts.reduceLeft(_+" AND "+_) + "\""
    } else {
      "\""+searchTerm+"\""
    }
  }

  override def toSPARQL(searchTerm : String) = {
    "?" + key + " " + sparqlUri + " ?containsText ." +
    "?containsText " +containsUri.sparqlUri+" "+formatSearchTerm(searchTerm)+ " .  "
  }

  override def toXML = 
    <search-predicate>
      <uri>{xmlUri}</uri>
      <weight>{weight.toString}</weight>
      <method>contains</method>
      <contains-uri>{containsUri.xmlUri}</contains-uri>
    </search-predicate>
}

object SearchPredicate {

  /**
   * Takes a <search-predicate> node of a configuration and returns the
   * corresponding SearchPredicate instance.
   */
  def apply(predNode : scala.xml.Node) : SearchPredicate = {
    
    /* Get the predicates to use */
    var predicates = new HashMap[String, SearchPredicate]()
    
    val uri = (predNode\"uri").text
    val weight = 
      if((predNode\"weight").isEmpty)
        0
      else
        (predNode\"weight").text.toFloat
    
    val method = 
      if((predNode\"method").isEmpty)
        "exact"
      else
        (predNode\"method").text
    
    val pred:SearchPredicate =
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