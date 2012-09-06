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
import br.ufrj.greco.casablanca.tools.TestProfileUtils._
import org.junit.Test
import scala.xml.XML

class AddProfileTest extends WebServiceTest {

  @Test
  def addProfileTest {
    val webRes = getWebResource("profiles/add")
    println("-- Before --")
    println(ProfileManager)

    for(p <- profiles.map(resourceAsXML(_).toString)) {
      val resp = webRes.`type`("text/xml").post(classOf[String], p)
      println("Reply : " + resp)
      val id = XML.loadString(resp).text.toInt
      (new ProfilesTest).checkProfileDescription(id)
    }

    println("-- After --")
    println(ProfileManager)
  }

}
