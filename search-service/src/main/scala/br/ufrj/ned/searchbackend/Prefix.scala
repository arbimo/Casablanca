package br.ufrj.ned.searchbackend

object Prefix {

  val prefixes = new collection.mutable.HashMap[String, String] 

  prefixes += ("rdfs" -> "http://www.w3.org/2000/01/rdf-schema#") 
  prefixes += ("rdf" -> "http://www.w3.org/1999/02/22-rdf-syntax-ns#") 
  prefixes += ("yago" -> "http://yago-knowledge.org/resource/")
  prefixes += ("owl" -> "http://www.w3.org/2002/07/owl#") 
  prefixes += ("xsd" -> "http://www.w3.org/2001/XMLSchema#") 
  prefixes += ("dbres" -> "http://dbpedia.org/resource/") 
  prefixes += ("dbonto" -> "http://dbpedia.org/ontology/") 
  prefixes += ("foaf" -> "http://xmlns.com/foaf/0.1/") 
  prefixes += ("bif" -> "bif:") 

  def apply(pref:String) : String = {
    if(!prefixes.contains(pref))
      throw new Exception("Prefix "+pref+" is not is the available one.")
    prefixes(pref)
  }
}
