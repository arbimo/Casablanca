package br.ufrj.ned.searchbackend

import com.hp.hpl.jena.query._
import scala.collection.JavaConversions._

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

   /** Limit the size of the results.
    *  This should be avoided not to miss pretinent results
    */
    private val limit = " "
    
    
    /**  This function creates a Jena ARQ Query from :
     *
     * @param searchTerm string to look for
     * @param backend a SearchBackend. This describes the dataset to use
     * @return a Jena ARQ Query
     */
    def create(searchTerm : String, backend : SearchBackend) : Query = {

      var structBegin = "SELECT "
      for(p <- backend.predicates.values)
        structBegin += "?" + p.key + " "

      structBegin += " WHERE { "
      
      var body = ""
      
      val it = backend.predicates.valuesIterator
      while(it.hasNext) {
        val p = it.next
        body += " { "

        /* search part*/
        body += p.toSPARQL(searchTerm)

        /* type constraint */
        for(typeUri <- backend.types)
          body += "?" + p.key + " a " + typeUri + " . "
      
        body += " } "
        if(it.hasNext)
          body += " UNION "
      }
      
      var structEnd = " } "
      
      var query = prefix + structBegin + body + structEnd + limit
      
      return QueryFactory.create(query)
    }

}