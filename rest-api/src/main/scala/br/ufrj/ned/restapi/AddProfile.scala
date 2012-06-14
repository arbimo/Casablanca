package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.exceptions._

@Path("/profiles/add")
class AddProfile {

  @GET
  @Produces(Array("text/xml"))
  def addProfile = {
    <coucou>dsdsd</coucou>.toString
  }

  @POST
  @Consumes(Array("application/xml"))
  @Produces(Array("text/xml"))
  def addProfile(xmlString:String) = {
    try {
      println("dddddddddddddd"+xmlString)

      val id = ProfileManager.addProfile(scala.xml.XML.loadString(xmlString))
      <id>{id}</id>
    } catch {
      case e:InvalidProfileException =>
        throw new InvalidProfileWebException
    }
  }
}
