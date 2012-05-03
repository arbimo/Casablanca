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
  
  log.info("Running UFRJ-NER")

  val file = "/home/arthur/Info/ScalaJena/src/ressources/default.endpoint"
  
  val sbf = new SearchBackendFactory
  val sb = sbf.createFromFile(file) match { case Some(backend) => backend
                                   case None => exit(-1) }
  
  val results = sb.search("Will")
  println(results)
  while(results.hasNext) {
    val res = results.next()
    println(res)
  }
}
