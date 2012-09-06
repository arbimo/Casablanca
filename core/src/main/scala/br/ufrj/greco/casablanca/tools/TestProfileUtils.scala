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
 
package br.ufrj.greco.casablanca.tools

import br.ufrj.greco.casablanca.searchbackend.SearchProfile
import org.xml.sax.InputSource

/**
 * This singleton object is provides a few usefull methods and attributes
 * usefull for testing. They are not supposed to be used in production since
 * they use assertions to make sure everything goes well.
 */
object TestProfileUtils {

  /**
   * A list of profiles stored as resources.
   * Those are supposed to be valid every time.
   */
  val profiles = List("/profiles/dbpedia.xml", 
                      "/profiles/dbpedia-cities.xml",
                      "/profiles/local-yago.xml",
                      "/profiles/local-yago-cities.xml")

  /**
   * Return an XML representation of the resource corresponding to 
   * the given URI.
   * 
   * *Warning* : if the URI is not valid, this will fail on assertion.
   */
  def resourceAsXML(URI:String) = {
    val configIn = this.getClass.getResourceAsStream(URI)
    assert(configIn != null, "Profile resource not found")
    scala.xml.XML.load(new InputSource(configIn))
  }

  /**
   * Return a SearchProfile representation of the resource corresponding to 
   * the given URI.
   * 
   * *Warning* : if the URI or XML config is not valid, 
   * this will fail on assertion.
   */
  def searchProfileFromResource(URI:String) :SearchProfile = {
    val sp = SearchProfile(resourceAsXML(URI))
    assert(sp.isInstanceOf[Some[SearchProfile]],
           "Profile "+sp+" not valid")
    sp.asInstanceOf[Some[SearchProfile]].get
  }


}
