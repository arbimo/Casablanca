package br.ufrj.ned.restapi

import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import br.ufrj.ned.profilemanager.ProfileManager
import com.sun.jersey.api.client.Client
import org.glassfish.grizzly.http.server.HttpServer
import org.junit.Before
import org.junit.After

trait WebServiceTest extends AssertionsForJUnit {

  var server : HttpServer = null;

  def getWebResource(service:String) = {
    Client.create.resource(SearchServer.BASE_URI.toString +"/"+ service)
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
