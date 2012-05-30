package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.backendmanager._
import javax.ws.rs.core.Response

@Path("/search/{searchTerm}")
class SearchWS {

  @GET @Produces(Array("text/xml"))
  def doGet(@PathParam("searchTerm") searchTerm:String,
            @DefaultValue("-1") @QueryParam("profile") profile:Int) = {
    val backendOption = {
      if(profile == -1)
        BackendManager !? RetrieveDefault
      else
        BackendManager !? RetrieveBackend(profile)
    }
    val sb = backendOption match {
      case sb:SearchBackend => sb 
      case _ => throw new WebApplicationException(
        Response
          .status(Response.Status.BAD_REQUEST)
          .entity("Profile id is not valid : " + profile)
          .build())
    }
    val resultSeq = sb.search(searchTerm)
    (<search term={searchTerm}>{resultSeq.map(res => res.toXML)}</search>).toString
  }

  
}