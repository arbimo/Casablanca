package br.ufrj.ned.searchbackend

import br.ufrj.ned.exceptions._
import com.codahale.logula.Logging
import com.hp.hpl.jena.query._
import com.hp.hpl.jena.sparql.engine.http.QueryExceptionHTTP
import scala.collection.JavaConversions._
import scala.collection.mutable.HashMap

/**
 * This class provides to way to produce and execute a SPARQL query that is likely
 * to be valid on any SPARQL EndPoint (i.e. compatible with SPARQL 1.0).
 * 
 * While this is provided for a wider compatibility, most advanced methods 
 * should be used to get improved performances.
 */
class BasicSparqlBackend extends SearchBackend with Logging {

  override def search(searchTerm:String, profile:SearchProfile) = {
    try {
      log.info("Searching for \"%s\" on <%s>", searchTerm, profile.queryUrl)
      
      val query = createQuery(searchTerm, profile)
      
      log.info("Remote query execution start")
      val rawResults = QueryExecutionFactory.sparqlService(profile.queryUrl, query).execSelect()
      log.info("Remote query execution end")
      
      val results = treatResults(rawResults, profile)
      
      results.toOrderedSearchResults
    } catch {
      case e:QueryExceptionHTTP =>
        throw new RemoteEndPointException(e.toString)
    }
  }

  /** 
   * Takes a ResultSet and creates a Seq of SearchResults weighted with 
   * their match score.
   * 
   */
  private def treatResults(results : ResultSet, profile:SearchProfile) : SearchResultSet = {
    val popularitiesMap = new HashMap[String, PopularityMethod]()
    for(pop <- profile.popularities)
      popularitiesMap.update(pop.key, pop)

    /**
     * list of the keys that are used to reference the different scores
     */
    val scoreKeys = popularitiesMap.keySet + "match"
    val propKeys = 
      for(p <- profile.properties.toSet[PropertyPredicate]) yield p.key
    
    val candidates = new SearchResultSet(profile.popularities.toSet, profile.properties.toSet)
    
    for(sol <- results ; varName <- sol.varNames()) {
      /* if varName match to one of our search predicates */
      if(profile.predicates.contains(varName)) {
        val uri = sol.getResource(varName).toString
        val pred = profile.predicates(varName)
        
        val oldScore = candidates.getScore(uri, "match")

        candidates.setScore(uri, "match", oldScore + pred.weight)

        for(key <- sol.varNames) {
          if(popularitiesMap.contains(key))
            candidates.setScore(uri, key, sol.getLiteral(key).getFloat)
          else if(propKeys.contains(key))
            candidates.addProp(uri, key, sol.get(key).toString)
        }
      }
    }
    
    candidates
  }
  
  /**  
   * This function creates a Jena ARQ Query from a search term and a SearchProfile.
   * 
   * The resulting query contains one variable per search predicate and one variable 
   * per popularity measure.
   *
   * @param searchTerm string to look for
   * @param profile a SearchProfile. This describes the dataset to use
   * @return a Jena ARQ Query
   */
  def createQuery(searchTerm : String, profile : SearchProfile) : Query = {
    
    var structBegin = "SELECT "
    /* add the predicate keys */
    for(p <- profile.predicates.values)
      structBegin += "?" + p.key + " "

    /* Add the popularity keys */
    for(pop <- profile.popularities)
      structBegin += "?"+pop.key+" "

    /* Add the property keys */
    for(prop <- profile.properties)
      structBegin += "?"+prop.key+" "

    structBegin += " WHERE { "
    
    var body = ""
    
    val it = profile.predicates.valuesIterator
    while(it.hasNext) {
      val p = it.next
      body += " { "
      
      /* search part*/
      body += p.toSPARQL(searchTerm)
      
      /* type constraint */
      for(typeSeq <- profile.types) {
        body += " { "
        val typeIt = typeSeq.iterator
        while(typeIt.hasNext) {
          val typeUri = typeIt.next
          body += " { ?" + p.key + " a " + typeUri.sparqlUri + " . } "
          if(typeIt.hasNext)
            body += " UNION "
        }
        body += " } "
      }
      /* popularity measure */
      for(pop <- profile.popularities)
        body += " OPTIONAL { " + pop.toSparql("?"+p.key) +" } "

      /* properties */
      for(prop <- profile.properties)
        body += " OPTIONAL { " + prop.toSparql("?"+p.key) +" } "
      
      body += " } "
      if(it.hasNext)
        body += " UNION "
    }
    
    var structEnd = " } "
    
    var query = prefix + structBegin + body + structEnd + limit

    return QueryFactory.create(query)
  }
  
}
