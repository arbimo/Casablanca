package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.exceptions._

@Path("/profiles/{profileId}")
class Profile {
  
  @GET
  @Produces(Array("text/xml"))
  def getProfile(@PathParam("profileId") profileId:Int) = {

    try {
      ProfileManager.retrieveProfile(profileId).toXML.toString
    } catch {
      case e:ProfileNotFoundException => 
        throw new ProfileNotFoundWebException(profileId)
    }
  }

}
