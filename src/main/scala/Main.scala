/**
 * Created with IntelliJ IDEA.
 * User: arthur
 * Date: 4/27/12
 * Time: 11:55 AM
 * To change this template use File | Settings | File Templates.
 */

import com.hp.hpl.jena.query._

object Main extends App {
  println("Hello World")

  var query = DBPediaSQF.create("Casablanca")

  // initializing queryExecution factory with remote service.
  // **this actually was the main problem I couldn't figure out.**
  //var qexec = QueryExecutionFactory.sparqlService("http://dbpedia.org/sparql", query);
  var qexec = QueryExecutionFactory.sparqlService(DBPedia.url, query);


  val results = qexec.execSelect();
  while(results.hasNext) {
    val res = results.next()
    println(res)
  }

}
