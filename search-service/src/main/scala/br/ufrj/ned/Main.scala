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
  
  override def main(args : Array[String]) = {
    initLogging
    log.info("Running UFRJ-NER")

    // TODO: cleaner startup process
    //BackendManager.start()

    try {
      val searchTerm =
        if(args.length >= 1)
          args.last
        else
          throw new IllegalArgumentException("No search term provided")
    
      // TODO: cleaner startup process
      //BackendManager ! new LoadFromDir(System.getenv("UFRJ_NED_CONF"))

      val backendOption = BackendManager !? RetrieveDefault
      val sb = backendOption match {case sb:SearchBackend => sb }

      val results = sb.search(searchTerm)
      results.reverse.foreach(result => println(result))

    } catch {
      case e: IllegalArgumentException =>
        println(e)
        println("Unvalid arguments, use the following schema :")
        println("run [ config-file ] search-term")
        log.error("Arguments are not valid (%s)", args.mkString(" "))
    } finally {
      BackendManager ! 'quit
    }
    
  }
}
