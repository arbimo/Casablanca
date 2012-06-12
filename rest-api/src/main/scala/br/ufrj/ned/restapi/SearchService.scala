package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.exceptions._

@Path("/search/{searchTerm}")
class SearchWS {

  @GET @Produces(Array("text/xml"))
  def doGet(@PathParam("searchTerm") searchTerm:String,
            @DefaultValue("-1") @QueryParam("profile") profileId:Int) = {
    try {
      val sb = {
        if(profileId == -1)
          ProfileManager.retrieveDefault
        else
          ProfileManager.retrieveProfile(profileId)
      }
      val resultSeq = sb.search(searchTerm)
      (<search term={searchTerm}>{resultSeq.map(res => res.toXML)}</search>).toString
      
    } catch {
      case e:ProfileNotFoundException => 
        throw new ProfileNotFoundWebException(profileId)
      case e:RemoteEndPointException =>
        throw new RemoteEndPointWebException(e.toString)
        
    }
  }
  
  
}