package br.ufrj.ner;


import com.hp.hpl.jena.query._
import br.ufrj.ner.SearchBackend._

import com.codahale.logula.Logging
import org.apache.log4j.Level


object Main extends App with Logging {
  
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
  
  override def main(args : Array[String]) = {
    log.info("Running UFRJ-NER")
    
    var configFile = ""
    var searchTerm = ""
    
    if(args.length == 1) {
      configFile = "/home/arthur/Info/ScalaJena/src/ressources/default.endpoint"
      searchTerm = args(0)
    } else if(args.length == 2) {
      configFile = args(0)
      searchTerm = args(1)
    } else {
      println("run [ config-file ] search-term")
      log.error("Arguments are not valid (%s)", args.mkString)
      exit(1)
    }

    val sbf = new SearchBackendFactory
    val sb = sbf.createFromFile(configFile) match { 
      case Some(backend) => backend
      case None => exit(-1) 
    }
  
    val results = sb.search(searchTerm)
    println(results)
    while(results.hasNext) {
      val res = results.next()
      println(res)
    }
  }
}
