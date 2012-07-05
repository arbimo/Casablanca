package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.tools.TestProfileUtils._
import br.ufrj.ned.exceptions._
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