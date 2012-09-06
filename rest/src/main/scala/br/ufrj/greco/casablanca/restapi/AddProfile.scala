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

@Path("/profiles/add")
class AddProfile extends WebService {

  @POST
  @Consumes(Array("text/xml"))
  @Produces(Array("text/xml"))
  def addProfile(xmlString:String) = {
    try {
      val id = ProfileManager.addProfile(scala.xml.XML.loadString(xmlString))
      ok(<id>{id}</id>.toString)
    } catch {
      case e:InvalidProfileException =>
        throw new InvalidProfileWebException
    }
  }
}
