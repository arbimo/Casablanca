package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.exceptions._
import br.ufrj.ned.tools.TestProfileUtils._
import org.junit.Test
import scala.xml.XML

class AddProfileTest extends WebServiceTest {

  @Test
  def addProfileTest {
    val webRes = getWebResource("profiles/add")
    println("-- Before --")
    println(ProfileManager)

    for(p <- profiles.map(resourceAsXML(_).toString)) {
      val resp = webRes.`type`("text/xml").post(classOf[String], p)
      println("Reply : " + resp)
      val id = XML.loadString(resp).text.toInt
      (new ProfilesTest).checkProfileDescription(id)
    }

    println("-- After --")
    println(ProfileManager)
  }

}
