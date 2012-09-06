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

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import br.ufrj.greco.casablanca.profilemanager.ProfileManager
import com.sun.jersey.api.client.Client
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.Before
import org.junit.After

trait WebServiceTest extends AssertionsForJUnit {

  var server : HttpServer = null;

  def getWebResource(service:String) = {
    Client.create.resource(SearchServer.baseURI.toString +"/"+ service)
  }
  
  @Before
  def createServer() {
    server = SearchServer.startServer
  }

  @After
  def stopServer {
    server.stop
  }

  @After
  def clearProfiles {
    ProfileManager.clearAll
  }

}
