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

import com.codahale.logula.Logging
import com.sun.jersey.api.container.grizzly2.GrizzlyServerFactory;
import com.sun.jersey.api.core.PackagesResourceConfig;

import javax.ws.rs.core.UriBuilder;

import br.ufrj.greco.casablanca.profilemanager._
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

  private var domain = "http://localhost/"
  private var port = 9998
  
  def baseURI = {
    UriBuilder.fromUri(domain).port(getPort(port))
            .path("casablanca").build();
  }
  
  def startServer = {
    val rc = new PackagesResourceConfig("br.ufrj.greco.casablanca.restapi");
    
    println("Starting grizzly...");
    GrizzlyServerFactory.createHttpServer(baseURI, rc);
  }

  def main(args: Array[String]) {
    initLogging
    
    /* Starting Profile manager */
    if(System.getenv("CASABLANCA_PROFILES_DIR") != null)
      ProfileManager.loadFromDir(System.getenv("CASABLANCA_PROFILES_DIR"))

    if(args.length >= 1) {
      domain = 
        if(args(0).startsWith("http://"))
          args(0)
        else
          "http://"+args(0)
      if(args.length >= 2)
        port = args(1).toInt
    }
    
    val httpServer = startServer
    
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
