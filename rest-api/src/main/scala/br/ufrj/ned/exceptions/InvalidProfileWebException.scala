package br.ufrj.ned.exceptions

import javax.ws.rs.WebApplicationException
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status._

class InvalidProfileWebException
  extends WebApplicationException (
    Response.status(BAD_REQUEST).entity("Profile doesn't seems to be valid.")
            .`type`(MediaType.TEXT_PLAIN).build)
