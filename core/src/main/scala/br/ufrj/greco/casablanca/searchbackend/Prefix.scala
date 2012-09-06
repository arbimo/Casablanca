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
 
package br.ufrj.greco.casablanca.searchbackend

/**
 * This object provides a way to store and access prefixes.
 * 
 * This is needed since the XML format used for profile definition doesn't 
 * provide a native support for prefixes
 */
object Prefix {

  /**
   * Stores the prefixes available in the application
   */
  val prefixes = new collection.mutable.HashMap[String, String] 

  prefixes += ("rdfs" -> "http://www.w3.org/2000/01/rdf-schema#") 
  prefixes += ("rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#") 
  prefixes += ("yago" -> "http://yago-knowledge.org/resource/")
  prefixes += ("owl" -> "http://www.w3.org/2002/07/owl#") 
  prefixes += ("xsd" -> "http://www.w3.org/2001/XMLSchema#") 
  prefixes += ("dbres" -> "http://dbpedia.org/resource/") 
  prefixes += ("dbonto" -> "http://dbpedia.org/ontology/") 
  prefixes += ("foaf" -> "http://xmlns.com/foaf/0.1/") 
  prefixes += ("bif" -> "bif:") 

  /**
   * Returns the full URI corresponding to the prefix.
   * If none exist, an Exception is thrown.
   */
  def apply(pref:String) : String = {
    if(!prefixes.contains(pref))
      throw new Exception("Prefix "+pref+" is not is in the available ones.")
    prefixes(pref)
  }
}
