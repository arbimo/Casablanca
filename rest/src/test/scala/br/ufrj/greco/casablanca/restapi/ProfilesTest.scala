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
 
package br.ufrj.greco.casablanca.restapi

import javax.ws.rs._
import br.ufrj.greco.casablanca.profilemanager._
import br.ufrj.greco.casablanca.searchbackend._
import br.ufrj.greco.casablanca.tools.TestProfileUtils._
import br.ufrj.greco.casablanca.exceptions._
import javax.ws.rs.core.MediaType
import org.junit.Assert._
import org.junit.Test
import scala.xml.XML

class ProfilesTest extends WebServiceTest {

  def populateProfiles {
    for(p <- profiles) {
      val index = ProfileManager.addProfile(resourceAsXML(p))
    }
  }

  /**
   * Retrieves the XML of a load profile using the appopriae web service 
   * (profiles/{id}).
   */
  def checkProfileDescription(id:Int) {
    val webRes = getWebResource("profiles/"+id.toString)
    val result = webRes.get(classOf[String])
    val searchProfile = SearchProfile(XML.loadString(result))
    assert(searchProfile.isInstanceOf[Some[SearchProfile]],
            "Retrieved configuration couldn't be parsed")

    val sp = searchProfile.get
    assert(sp.name === ProfileManager.retrieveProfile(id).name,
            "Name of the profiles are not identical")
  }

  @Test
  def getProfileListXMLTest {
    val webRes = getWebResource("profiles")
    populateProfiles
    val result = webRes.get(classOf[String])
    assertNotNull(result);
    val xml = XML.loadString(result)
    val numProfiles = (xml\"profile").length
    assert(numProfiles === profiles.length, "Some profiles weren't loaded")
  }

  @Test
  def getProfileListJSONTest {
    val webRes = getWebResource("profiles").accept(MediaType.APPLICATION_JSON)
    populateProfiles
    val result = webRes.get(classOf[String])
    assertNotNull(result);
  }

  @Test
  def getProfileDescriptionTest {
    for(p <- profiles) {
      val index = ProfileManager.addProfile(resourceAsXML(p))
      checkProfileDescription(index)
    }
  }
}