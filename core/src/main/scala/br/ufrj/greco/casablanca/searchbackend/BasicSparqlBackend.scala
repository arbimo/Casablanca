/**
 * Copyright 2012 Arthur Bit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package br.ufrj.greco.casablanca.searchbackend

import br.ufrj.greco.casablanca.searchbackend.resources._
import br.ufrj.greco.casablanca.searchbackend.searchcomponents._
import br.ufrj.greco.casablanca.exceptions._
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
    val popularitiesMap = new HashMap[String, Popularity]()
    for(pop <- profile.specialization ; if(pop.isInstanceOf[Popularity]))
      popularitiesMap.update(pop.key, pop.asInstanceOf[Popularity])

    /**
     * list of the keys that are used to reference the different scores
     */
    val scoreKeys = popularitiesMap.keySet + "match"
    val propKeys = 
      for(p <- profile.specialization.toSet[SpecializationComponent] ;
          if(p.isInstanceOf[Property])) yield p.key
    
    val candidates = new SearchResultSet(
      profile.popularities.toSet,
      profile.properties.toSet)
    
    for(sol <- results ; varName <- sol.varNames()) {
      /* if varName match to one of our search predicates */
      if(profile.predicates.contains(varName)) {
        val uri = sol.getResource(varName).toString
        val pred = profile.predicates(varName)
        
        val oldScore = candidates.getScore(uri, "match")

        candidates.setScore(uri, "match", oldScore + pred.weight)

        for(key <- sol.varNames) {
          if(popularitiesMap.contains(key)) {
            try {
              candidates.setScore(uri, key, sol.getLiteral(key).getFloat)
            } catch {
              case _ => log.error("Unable to convert score to float."+
                "A Popularity measure might return a non-numeric result")
            }
          }
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

    /* Add the popularities and properties keys */
    for(spec <- profile.specialization ; if(spec.result != None))
      structBegin += spec.selectString + " "

    structBegin += " WHERE { "
    
    var body = ""
    
    val it = profile.predicates.valuesIterator
    while(it.hasNext) {
      val p = it.next
      body += " { "
      
      /* search part*/
      body += p.toSPARQL(searchTerm)
      
      /* specialization lines */
      for(spec <- profile.specialization)
        body += spec.toSparql(new Variable(p.key))
      
      body += " } "
      if(it.hasNext)
        body += " UNION "
    }
    
    var structEnd = " } GROUP BY "
    /* add the predicate keys */
    for(p <- profile.predicates.values)
      structEnd += "?" + p.key + " "

    /* Add the popularities and properties keys */
    for(spec <- profile.specialization ; if(spec.groupBy))
      structEnd += " ?"+spec.key 
    
    var query = structBegin + body + structEnd + limit

    try {
      return QueryFactory.create(query)
    } catch {
      case e => {
          log.error("Problem loading query :\n" + query)
          throw e
        }
    }
  }
  
}
