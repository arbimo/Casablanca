package br.ufrj.ned.restapi

import com.codahale.logula.Logging
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;

import javax.ws.rs.core.UriBuilder;

import br.ufrj.ned.profilemanager._
import org.apache.log4j.Level



object SearchServer extends Logging {

  def initLogging() = {
    Logging.configure { log =>
      log.registerWithJMX = true

      log.level = Level.INFO
      log.loggers("ufrj.ned") = Level.OFF

      log.console.enabled = true
      log.console.threshold = Level.WARN

      log.file.enabled = true
      log.file.filename = "/tmp/ufrj-ned.log"
      log.file.maxSize = 10 * 1024 // KB
      log.file.retainedFiles = 5 // keep five old logs around

      // syslog integration is always via a network socket
      log.syslog.enabled = false
    }
  }

   private def getPort(defaultPort : Int) = {
     val port = System.getProperty("jersey.test.port");;
    
    if (null != port)
      try  {
        Integer.parseInt(port);
      } catch {
        case ex: NumberFormatException => defaultPort;
      }
    else
      defaultPort;
  }
  
  private def getBaseURI = {
    UriBuilder.fromUri("http://localhost/").port(getPort(9998))
            .path("ned").build();
  }
  
  val BASE_URI = getBaseURI();
  
  def startServer = {
    val rc = new PackagesResourceConfig("br.ufrj.ned.restapi");
    
    println("Starting grizzly...");
    GrizzlyServerFactory.createHttpServer(BASE_URI, rc);
  }
  
  def main(args: Array[String]) {
    initLogging
    
    /* Starting Profile manager */
    if(System.getenv("UFRJ_NED_CONF") != null)
      ProfileManager.loadFromDir(System.getenv("UFRJ_NED_CONF"))
    
    val httpServer = startServer()
    
    println("Server running")
    println("Hit return to stop...")
    System.in.read()
    println("Stopping server")
    httpServer.stop()
    ProfileManager.stop
    println("Server stopped")
    System.exit(0);
  }
}
