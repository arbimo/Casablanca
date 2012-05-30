package br.ufrj.ned;


import com.hp.hpl.jena.query._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.backendmanager._

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

    try {
      var exit = false
      println(helpString)

      /* Starting Backend manager */
      BackendManager.start()
      if(!System.getenv("UFRJ_NED_CONF").isEmpty)
        BackendManager ! new LoadFromDir(System.getenv("UFRJ_NED_CONF"))
    
      while(!exit) {
        print("> ")
        val cmd = readLine

        if(cmd == null || cmd == "exit") {
          exit = true
        } else if(cmd == "help") {
          println(helpString)
        } else if(cmd == "list") {
          println(BackendManager)
        } else if(cmd.startsWith("load ")) {
          val id = cmd.drop(("load ").length).toInt
          BackendManager ! new SetDefault(id)
          println("Loading backend " + id)
          
        } else if(cmd.startsWith("search ")) {
          val searchTerm = cmd.drop(("search ").length)

          val backendOption = BackendManager !? RetrieveDefault
          val sb = backendOption match {case sb:SearchBackend => sb }
          println("Looking for \""+searchTerm+"\" on "+sb.name)
          
          val results = sb.search(searchTerm)
          results.reverse.foreach(result => println(result))
        } else if(cmd.startsWith("request ")) {
          val searchTerm = cmd.drop(("request ").length)
          val backendOption = BackendManager !? RetrieveDefault
          val sb = backendOption match {case sb:SearchBackend => sb }
          println(SearchQueryFactory.create(searchTerm, sb))
        } else {
          println("This was not a valid command")
          println(helpString)
        }
      }
    } catch {
      case e: IllegalArgumentException =>
        println(e)
        log.error("Arguments are not valid (%s)", args.mkString(" "))
    } finally {
      BackendManager ! 'quit
    }
  }
}
