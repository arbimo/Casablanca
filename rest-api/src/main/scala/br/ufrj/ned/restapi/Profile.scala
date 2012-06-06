package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.backendmanager._
import br.ufrj.ned.backendmanager.exceptions._
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status._

@Path("/profiles/{id}")
class Profile {

  
  @GET
  @Produces(Array("text/xml"))
  def getProfile(@PathParam("id") id:Int) = {

    try {
      BackendManager.retrieveBackend(id).toXML.toString
    } catch {
      case e:ProfileNotFoundException => throw new WebApplicationException(
        Response
          .status(Response.Status.BAD_REQUEST)
          .entity("Id is not valid : "+id)
          .build())
    }

  }
  

}
