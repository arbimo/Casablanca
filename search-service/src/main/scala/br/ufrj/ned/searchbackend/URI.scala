package br.ufrj.ned.searchbackend

import com.codahale.logula.Logging

/**
 * This class is a container for the URI of RDF entities.
 * 
 * Its main goal is to provide method to generate both SPARQL and XML
 * valid versions of the URI.
 */
class URI(rawURI : String) extends Logging {

  /** 
   * Normalize a URI for use in SPARQL.
   * 
   * @param uriText URI to normalize
   * @return <uriText> if the URI doesn't use a prefix, uriText otherwise
   */
  def normalizeUri(uriText : String) : String = {
    def clean(uri:String) = 
      if(uri.contains("\"")) {
        log.warn("This URI contains double quotes : "+uri)
        uri.replaceAll("\"", "")
      } else
        uri

    if(uriText.startsWith("http://") || uriText.startsWith("bif:"))
      "<"+clean(uriText)+">"
    else
      clean(uriText)
  }
  
  /**
   * A version of the URI that is compatible for SPARQL queries.
   */
  def sparql = normalizeUri(rawURI) 

  /**
   * A version of the URI that can be inserted in a valid XML document.
   */
  def xml =
    if(rawURI.startsWith("<") && rawURI.endsWith(">"))
      rawURI.drop(1).dropRight(1)
    else
      rawURI
  
  override def toString = this.xml
  
}
