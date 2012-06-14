package br.ufrj.ned.searchbackend

import br.ufrj.ned.profilemanager.ProfileUtils._
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test

class SearchProfileTest extends AssertionsForJUnit {

  
  @Test
  def createProfileFromXMLTest() {
    for(prof <- profiles)
      assert(SearchProfile(resourceAsXML(prof)).isInstanceOf[Some[SearchProfile]],
        "Profile "+prof+" not valid")
  }

  @Test
  def searchOnDBPediaProfileTest() {
    val prof = SearchProfile(resourceAsXML("/profiles/dbpedia-cities.xml"))
    assert(prof != None, "Profile Resource should exist")
    for(profile <- prof) {
      assert(profile.search("Casablanca").length != 0, "Some results were expected")
    }
  }

  @Test
  def XMLSerializationTest() {
    for(profURI <- profiles) {
      val sp = SearchProfile(resourceAsXML(profURI))
                 .asInstanceOf[Some[SearchProfile]].get
      val spClone = SearchProfile(sp.toXML)
                 .asInstanceOf[Some[SearchProfile]].get
      assert(sp.toXML == spClone.toXML)
    }
  }
  
}
