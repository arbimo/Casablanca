package br.ufrj.ned.searchbackend.resources

import br.ufrj.ned.searchbackend._
import com.codahale.logula.Logging

/**
 * This class is a container for the URI of RDF entities.
 * 
 * Its main goal is to provide method to generate both SPARQL and XML
 * valid versions of the URI.
 */
class URI(rawURI : String) extends Resource with Logging {

  val fullURI = {
    if(rawURI.startsWith("<") && rawURI.endsWith(">"))
      rawURI
    else if(rawURI.startsWith("http://"))
      normalizeURI(rawURI)
    else {
      val prefix = rawURI.dropRight(rawURI.length - rawURI.indexOf(":"))
      normalizeURI(Prefix(prefix)+rawURI.drop(rawURI.indexOf(":")+1))
    }
  }

  require(URI.isValid(fullURI), {log.error("URI \"%s\" is not valid", rawURI)})

  val sparqlUri = fullURI
  def toSparql = sparqlUri

  /**
   * A version of the URI that can be inserted in a valid XML document.
   */
  val xmlUri = fullURI.drop(1).dropRight(1)

  /** 
   * Normalize a URI for use in SPARQL.
   * 
   * @param uriText URI to normalize
   * @return <uriText> if the URI doesn't use a prefix, uriText otherwise
   */
  def normalizeURI(uriText : String) : String = {
    if(uriText.startsWith("http://") || uriText.startsWith("bif:"))
      "<"+uriText+">"
    else
      uriText
  }
  
  override def toString = this.xmlUri
  
}

object URI {

  /**
   * Checks the validity of an URI. (Double quotes etc ...)
   */
  def isValid(uri : String) : Boolean = {
    if(uri.contains("\"")) 
      false
    else
      true
    
  }
}
