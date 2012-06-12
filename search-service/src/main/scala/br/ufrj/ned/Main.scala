package br.ufrj.ned;


import br.ufrj.ned.searchbackend._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.exceptions._

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
    log.info("Running UFRJ-NED")

    var exit = false
    println(helpString)

    /* Starting Backend manager */
    ProfileManager.start()
    if(System.getenv("UFRJ_NED_CONF") != null)
      ProfileManager.loadFromDir(System.getenv("UFRJ_NED_CONF"))
    
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
          println(SearchQueryFactory.create(searchTerm, sb))
        } else {
          println("This was not a valid command")
          println(helpString)
        }
      } catch {
        case e:ProfileNotFoundException => println("Unable to find profile")
        case e:Exception => 
          println("Exception was raised during execution. Please check you command")
          println(e.toString)
      }
    }
    ProfileManager.stop
  }
}
