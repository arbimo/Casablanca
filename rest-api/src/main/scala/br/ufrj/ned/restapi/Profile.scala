package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.exceptions._
import net.liftweb.json.JsonAST._

@Path("/profiles/{profileId}")
class Profile extends WebService {

  override val jsonTransform : PartialFunction[JValue, JValue] = { 
    case JField("weight", JString(s)) => JField("weight", JDouble(s.toDouble))
    case JField("search-predicate", x: JObject) => JField("search-predicate", JArray(x :: Nil))
    case JField("measure", x: JObject) => JField("measure", JArray(x :: Nil))
    case JField("type-constraint", x: JObject) => JField("type-constraint", JArray(x :: Nil))
  }

  def retrieveProfile(profileId:Int) : scala.xml.NodeSeq = {
    try {
      ProfileManager.retrieveProfile(profileId).toXML
    } catch {
      case e:ProfileNotFoundException => 
        throw new ProfileNotFoundWebException(profileId)
    }
  }

  @GET
  @Produces(Array("application/xml"))
  def getProfileAsXML(@PathParam("profileId") profileId:Int) = {
    ok(retrieveProfile(profileId).toString)
    
  }

  @GET
  @Produces(Array("application/json"))
  def getProfileAsJSON(@PathParam("profileId") profileId:Int) = {
    ok(json(retrieveProfile(profileId)))
  } 

}
