package br.ufrj.ned.searchbackend

import br.ufrj.ned.tools.TestProfileUtils._
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

  @Test
  def searchOnDBPediaProfileTest {
    val prof = SearchProfile(resourceAsXML("/profiles/dbpedia-cities.xml"))
    assert(prof != None, "Profile Resource should exist")
    for(profile <- prof) {
      assert(profile.search("Casablanca").length != 0, "Some results were expected")
    }
  }

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
