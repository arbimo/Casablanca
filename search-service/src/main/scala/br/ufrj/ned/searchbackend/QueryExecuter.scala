package br.ufrj.ned.searchbackend

import com.codahale.logula.Logging
import com.hp.hpl.jena.query.QueryExecution
import scala.actors.Actor

/**
 * This class is a simple Actor to allow using a timeout on queries.
 * 
 * This was done because the methods setTimeout(...) on Jena's
 * QueryExecution objects are not implemented yet
 * Usage : queryExecuter !? (timeout, queryExecution)
 */
class QueryExecuter extends Actor with Logging {
  start()
  
  def act() {
    receive {
      case queryExec : QueryExecution =>
        println("Received query")
        reply(queryExec.execSelect)
    }
    
  }

}
