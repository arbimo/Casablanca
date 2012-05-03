package br.ufrj.ner.SearchBackend

import com.hp.hpl.jena.query._

object SearchQueryFactory {

    var prefix = 
        "PREFIX owl: <http://www.w3.org/2002/07/owl#> "  + 
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
        
    var structBegin = "SELECT ?entity WHERE { "
    
    var body = ""
    
    var structEnd = " } "
    
    var limit = " LIMIT 20 "
    
    

    def create(searchTerm : String, backend : SearchBackend) : Query = {
      
      structBegin = "SELECT " + backend.predicates(1).key + " WHERE { "
      
      body = backend.predicates(1).key + " " + backend.predicates(1).uri + " \"" + searchTerm + "\" . "
      
      var query = prefix + structBegin + body + structEnd + limit
      
      
      
      return QueryFactory.create(query)
    }

}