package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.searchbackend._
import net.liftweb.json.JsonAST._

@Path("/profiles/")
class Profiles extends WebService {

  override val jsonTransform : PartialFunction[JValue, JValue] = { 
    case JField("id", JString(s)) => JField("id", JInt(s.toInt))
    case JField("profile", x: JObject) => JField("profile", JArray(x :: Nil))
  }

  def retrieveProfiles() : scala.xml.NodeSeq = {
    val profiles = ProfileManager.getList

    <profiles>
      {for(i <- 0 to profiles.length-1) yield 
        <profile>
          <id>{i.toString}</id>
          <name>{profiles(i).name}</name>  
        </profile>
      }
    </profiles>
  }

  @GET
  @Produces(Array("application/xml"))
  def getProfilesAsXML() = {
    ok(retrieveProfiles.toString)
  }

  @GET
  @Produces(Array("application/json"))
  def getProfilesAsJSON() = {
    ok(json(retrieveProfiles))
  }


  
}