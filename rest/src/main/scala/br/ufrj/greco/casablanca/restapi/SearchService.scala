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

@Path("/search/{searchTerm}")
class SearchService extends WebService {

  override val jsonTransform : PartialFunction[JValue, JValue] = { 
    case JField("global-score", JString(s)) => JField("global-score", JDouble(s.toDouble))
    case JField("score", x:JObject) => JField("score", JArray(x :: Nil))
    case JField("search-result", x: JObject) => JField("search-result", JArray(x :: Nil))
    case JField("property", x: JObject) => JField("property", JArray(x :: Nil))
  }

  /**
   * Perform a search for the given search term on the given profile id.
   * If profileId is -1, the default profile is used.
   * 
   * Result is given as an XML serialization of SearchResults objects.
   */
  def performSearch(searchTerm:String, profileId:Int) : scala.xml.NodeSeq = {
    try {
      val sb = {
        if(profileId == -1)
          ProfileManager.retrieveDefault
        else
          ProfileManager.retrieveProfile(profileId)
      }
      val resultSeq = sb.search(searchTerm)
      
      <search term={searchTerm}>{resultSeq.map(res => res.toXML)}</search>
      
    } catch {
      case e:ProfileNotFoundException => 
        throw new ProfileNotFoundWebException(profileId)
      case e:RemoteEndPointException =>
        throw new RemoteEndPointWebException(e.toString)
    }
  }

  @GET @Produces(Array("application/xml"))
  def doGetXML(@PathParam("searchTerm") searchTerm:String,
            @DefaultValue("-1") @QueryParam("profile") profileId:Int) = {

    ok(performSearch(searchTerm, profileId).toString)
  }

  @GET @Produces(Array("application/json"))
  def doGetJSON(@PathParam("searchTerm") searchTerm:String,
            @DefaultValue("-1") @QueryParam("profile") profileId:Int) = {

    ok(json(performSearch(searchTerm, profileId)))
  }
  
  
}