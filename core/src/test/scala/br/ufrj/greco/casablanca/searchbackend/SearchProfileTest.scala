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

import br.ufrj.greco.casablanca.tools.TestProfileUtils._
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test

class SearchProfileTest extends AssertionsForJUnit {

  
  @Test
  def createProfileFromXMLTest {
    for(prof <- profiles)
      assert(SearchProfile(resourceAsXML(prof)).isInstanceOf[Some[SearchProfile]],
        "Profile "+prof+" not valid")
  }

  /** Disabled since it requires an Internet connection to succeed
  @Test
  def searchOnDBPediaProfileTest {
    val prof = SearchProfile(resourceAsXML("/profiles/dbpedia-cities.xml"))
    assert(prof != None, "Profile Resource should exist")
    for(profile <- prof) {
      assert(profile.search("Casablanca").length != 0, "Some results were expected")
    }
  }
  */

  @Test
  def XMLSerializationTest {
    for(profURI <- profiles) {
      val sp = searchProfileFromResource(profURI)
      val spCloneOption = SearchProfile(sp.toXML)
      spCloneOption match {
        case None =>
          println("Generated XML : \n"+sp.toXML.toString)
          println("Source XML : \n"+resourceAsXML(profURI).toString)
          fail(profURI + " - Unable to load generated XML.")
        case Some(spClone:SearchProfile) =>
          val sbClone = spCloneOption.asInstanceOf[Some[SearchProfile]].get
          assert(sp.toXML.mkString === spClone.toXML.mkString, "XMLs are not identical")
      }
        
    }
  }
  
}
