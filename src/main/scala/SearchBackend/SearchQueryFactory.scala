package br.ufrj.ner.SearchBackend

import com.hp.hpl.jena.query._

/** This object provides a way to create a Jena ARQ
 * query from search term and a search backend.
 *
 * It is an abstraction of jena.QueryFactory
 */
object SearchQueryFactory {

    private val prefix = 
        "PREFIX owl: <http://www.w3.org/2002/07/owl#> "  + 
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#> " +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#> " +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> "
        
    private val limit = " LIMIT 200 "
    
    
    /**  This function creates a Jena ARQ Query from :
     *
     * @param searchTerm string to look for
     * @param backend a SearchBackend. This describes the dataset to use
     * @return a Jena ARQ Query
     */
    def create(searchTerm : String, backend : SearchBackend) : Query = {
      
      var structBegin = "SELECT " 
      backend.predicates.foreach(p => structBegin += p.key)
      structBegin += " WHERE { "
      
      var body = ""
      for(i <- 0 to backend.predicates.length - 1) {
        val p = backend.predicates(i)
        body += "{ " + p.key + " " + p.uri + " \"" + searchTerm + "\" . } " 
        if(i != backend.predicates.length -1)
          body += " UNION "
      }
      
      var structEnd = " } "
      
      var query = prefix + structBegin + body + structEnd + limit
      
      return QueryFactory.create(query)
    }

}