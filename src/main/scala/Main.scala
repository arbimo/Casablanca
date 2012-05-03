package br.ufrj.ner;


import com.hp.hpl.jena.query._
import br.ufrj.ner.SearchBackend._


object Main extends App {
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
