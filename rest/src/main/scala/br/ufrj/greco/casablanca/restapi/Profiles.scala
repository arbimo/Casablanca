/**
 * Copyright 2012 Arthur Bit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package br.ufrj.greco.casablanca.restapi

import javax.ws.rs._
import br.ufrj.greco.casablanca.profilemanager._
import br.ufrj.greco.casablanca.searchbackend._
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