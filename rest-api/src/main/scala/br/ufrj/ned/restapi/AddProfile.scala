package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.exceptions._

@Path("/profiles/add")
class AddProfile {

  @POST
  @Consumes(Array("text/xml"))
  @Produces(Array("text/xml"))
  def addProfile(xmlString:String) = {
    try {
      val id = ProfileManager.addProfile(scala.xml.XML.loadString(xmlString))
      <id>{id}</id>.toString
    } catch {
      case e:InvalidProfileException =>
        throw new InvalidProfileWebException
    }
  }
}
