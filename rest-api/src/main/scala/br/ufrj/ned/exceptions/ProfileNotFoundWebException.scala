package br.ufrj.ned.exceptions

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status._

class ProfileNotFoundWebException(profileId:Int) 
  extends WebApplicationException (
    Response.status(BAD_REQUEST).entity("Requested profile wasn't found : " + profileId)
            .`type`(MediaType.TEXT_PLAIN).build)

