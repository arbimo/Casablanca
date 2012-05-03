package br.ufrj.ner;


import com.hp.hpl.jena.query._


object Main extends App {
  println("Hello World")

  var query = DBPediaSQF.create("Casablanca")

  // initializing queryExecution factory with remote service.
  var qexec = QueryExecutionFactory.sparqlService(DBPedia.url, query);


  val results = qexec.execSelect();
  while(results.hasNext) {
    val res = results.next()
    println(res)
  }

}
