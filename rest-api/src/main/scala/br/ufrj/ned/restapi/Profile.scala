package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.backendmanager._
import br.ufrj.ned.searchbackend._
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status._

@Path("/profiles/{id}")
class Profile {

  
  @GET
  @Produces(Array("text/xml"))
  def getProfile(@PathParam("id") id:Int) = {

    BackendManager !? new RetrieveBackend(id) match {
      case sb:SearchBackend => 
        sb.toXML.toString
      case _ => throw new WebApplicationException(
        Response
          .status(Response.Status.BAD_REQUEST)
          .entity("Id is not valid : "+id)
          .build())
    }

  }
  

}
