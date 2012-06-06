package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.backendmanager._
import br.ufrj.ned.searchbackend._
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status._

@Path("/profiles/")
class Profiles {

  @GET
  @Produces(Array("text/xml"))
  def getProfiles() = {
    val profiles = BackendManager.getList

    <profiles>
      {for(i <- 0 to profiles.length-1) yield 
        <profile>
          <id>{i.toString}</id>
          <name>{profiles(i).name}</name>  
        </profile>
      }
    </profiles>.toString
  }

  
}