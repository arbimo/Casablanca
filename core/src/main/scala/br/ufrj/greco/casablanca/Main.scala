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
 
package br.ufrj.greco.casablanca;


import br.ufrj.greco.casablanca.searchbackend.BasicSparqlBackend
import br.ufrj.greco.casablanca.searchbackend._
import br.ufrj.greco.casablanca.profilemanager._
import br.ufrj.greco.casablanca.exceptions._

import com.codahale.logula.Logging
import org.apache.log4j.Level


object Main extends App with Logging {
  
  def initLogging() = {
    Logging.configure { log =>
      log.registerWithJMX = true

      log.level = Level.INFO
      log.loggers("ufrj.ner") = Level.OFF

      log.console.enabled = true
      log.console.threshold = Level.WARN

      log.file.enabled = true
      log.file.filename = "/tmp/ufrj-ner.log"
      log.file.maxSize = 10 * 1024 // KB
      log.file.retainedFiles = 5 // keep five old logs around

      // syslog integration is always via a network socket
      log.syslog.enabled = false
    }
  }

  private def helpString =  "Usage : \n" +
    " - help     : print this help\n" +
    " - list     : list the different dataset availables\n" +
    " - load 3   : set the 3rd backend as the default\n" +
    " - search Casablanca   :  Search for Casablanca on the default backend\n" +
    " - request Casablanca  :  prints the request that bould be executed with \"search Casablanca\"\n"+
    " - exit     : quit the application\n"
  
  
  override def main(args : Array[String]) = {
    initLogging
    log.info("Running Casablanca")

    var exit = false
    println(helpString)

    /* Initializing profile manager */
    if(System.getenv("CASABLANCA_PROFILES_DIR") != null)
      ProfileManager.loadFromDir(System.getenv("CASABLANCA_PROFILES_DIR"))
    
    while(!exit) {
      try {
        print("> ")
        val cmd = readLine

        if(cmd == null || cmd == "exit") {
          exit = true
        } else if(cmd == "help") {
          println(helpString)
        } else if(cmd == "list") {
          println(ProfileManager)
        } else if(cmd.startsWith("load ")) {
          val id = cmd.drop(("load ").length).toInt
          ProfileManager.setDefault(id)
          println("Loading backend " + id)
          
        } else if(cmd.startsWith("search ")) {
          val searchTerm = cmd.drop(("search ").length)

          val sb = ProfileManager.retrieveDefault
          println("Looking for \""+searchTerm+"\" on "+sb.name)
          
          val results = sb.search(searchTerm)
          results.reverse.foreach(result => println(result))
        } else if(cmd.startsWith("request ")) {
          val searchTerm = cmd.drop(("request ").length)
          val sb = ProfileManager.retrieveDefault
          val backend = new BasicSparqlBackend
          println(backend.createQuery(searchTerm, sb))
        } else {
          println("This was not a valid command")
          println(helpString)
        }
      } catch {
        case e:ProfileNotFoundException => println("Unable to find profile")
        case e:Exception => 
          println("Exception was raised during execution. Please check you command")
          e.printStackTrace
      }
    }
    ProfileManager.stop
  }
}
