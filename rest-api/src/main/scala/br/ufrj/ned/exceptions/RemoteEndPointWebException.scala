package br.ufrj.ned.exceptions

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status._

class RemoteEndPointWebException(message:String) 
  extends WebApplicationException (
    Response.status(INTERNAL_SERVER_ERROR)
            .entity("Error with the remote SPARQL End Point : "+message)
            .`type`(MediaType.TEXT_PLAIN)
            .build)


