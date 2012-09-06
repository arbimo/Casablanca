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
import br.ufrj.greco.casablanca.searchbackend._
import br.ufrj.greco.casablanca.profilemanager._
import br.ufrj.greco.casablanca.exceptions._
import net.liftweb.json.JsonAST._

@Path("/profiles/{profileId}")
class Profile extends WebService {

  override val jsonTransform : PartialFunction[JValue, JValue] = { 
    case JField("weight", JString(s)) => JField("weight", JDouble(s.toDouble))
    case JField("search-predicate", x: JObject) => JField("search-predicate", JArray(x :: Nil))
    case JField("measure", x: JObject) => JField("measure", JArray(x :: Nil))
    case JField("type-constraint", x: JObject) => JField("type-constraint", JArray(x :: Nil))
  }

  def retrieveProfile(profileId:Int) : scala.xml.NodeSeq = {
    try {
      ProfileManager.retrieveProfile(profileId).toXML
    } catch {
      case e:ProfileNotFoundException => 
        throw new ProfileNotFoundWebException(profileId)
    }
  }

  @GET
  @Produces(Array("application/xml"))
  def getProfileAsXML(@PathParam("profileId") profileId:Int) = {
    ok(retrieveProfile(profileId).toString)
    
  }

  @GET
  @Produces(Array("application/json"))
  def getProfileAsJSON(@PathParam("profileId") profileId:Int) = {
    ok(json(retrieveProfile(profileId)))
  } 

}
