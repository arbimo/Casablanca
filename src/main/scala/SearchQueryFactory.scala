package br.ufrj.ner;

import com.hp.hpl.jena.query._

class SearchQueryFactory {

    var prefix = 
        "PREFIX owl: <http://www.w3.org/2002/07/owl#> "  + 
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
        
    var structBegin = "SELECT ?entity WHERE { "
    
    var body = ""
    
    var structEnd = " } "
    
    var limit = " LIMIT 20 "

    def create(searchTerm : String) : Query = {
        var query = prefix + structBegin + body + structEnd + limit
        return QueryFactory.create(query)
    }

}

object DBPediaSQF extends SearchQueryFactory {
    
    prefix += " PREFIX dbpedia: <http://dbpedia.org/property/> "
    
    override def create(searchTerm : String) : Query = {
        body = " ?entity rdfs:label ?label . " +
               "?label <bif:contains> \"" + searchTerm + "\" "
        
        val query = prefix + structBegin + body + structEnd + limit
        
        println("Here is the query : \n " + query)
        return QueryFactory.create(query)
    }
    
}