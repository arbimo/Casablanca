/**
 * Copyright 2012 Arthur Bit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package br.ufrj.greco.casablanca.searchbackend.resources

import br.ufrj.greco.casablanca.searchbackend._
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
  
  val typeStr = "URI"
  val value = this.xmlUri
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
