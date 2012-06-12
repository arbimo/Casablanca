package br.ufrj.ned.searchbackend

import scala.collection.JavaConversions._
import com.codahale.logula.Logging
import com.hp.hpl.jena.query.QueryExecution
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory

/**
 * This class aims at providing a way to retrieve the popularity associated with
 * an entity.
 * 
 * Every URI used here should be passed in a SPARQL usable format 
 * (i.e. <http://.../entity>)
 * Use SearchProfile.normalizeURI for that.
 */
class PopularityMeasurer(endPoint : String, method : PopularityMethod) extends Logging {

  private val popVariable = "?popularity"

  /**
   * Maximum number of variables to use in one query
   */
  private val maxVar = 300

  /**
   * Generates a simple query to retrieve the popularity of an entity
   * 
   * @param entity URI of the entity
   * @return QueryExecution which execution will give the corresponding popularity
   */
  private def queryFor(entity : URI) : QueryExecution = {
    val queryText = 
      "SELECT ?popularity WHERE {" +
    entity.sparql + " " + method.predicate.sparql + " "+popVariable +
      " } "

    val query = QueryFactory.create(queryText)
      
    QueryExecutionFactory.sparqlService(endPoint, query)
  }

  /**
   * Generates a Sequence of QueryExecution for the entities passed.
   * 
   * The split is done to avoid errors when executing queries with too many variables
   * @param entities A list of URIs for the entities to find
   * @return a Seq of QueryExecution
   */
  private def queriesFor(entities : Seq[URI]) : Seq[QueryExecution] = {
    val nbOfQueries = 
      if(entities.length % maxVar == 0)
        entities.length / maxVar
      else
        entities.length / maxVar + 1
    
    var queriesText = new Array[String](nbOfQueries)
    for(i <- 0 to entities.length-1) {
      val current = i/maxVar
      if(i%maxVar == 0) {
        queriesText(current) = "SELECT * WHERE {"
      }
      queriesText(current) += "OPTIONAL { "+ entities(i).sparql +" "+ method.predicate.sparql +" ?"+i +" } . " 

      if((i+1)%maxVar == 0 || i+1 == entities.length)
        queriesText(current) += " } "  
    }  
    val queries = queriesText map { QueryFactory.create(_) }
    queries map { QueryExecutionFactory.sparqlService(endPoint, _) }
  }
  
  /**
   * Returns a Seq of popularities associated with the entities passed as parameter
   * 
   * If no popularity is found, the popularity is set to 0
   * 
   * @param a Seq of URIs for the entities to search for
   * @return a Seq of popularities with the same index as the corresponding entity
   */
  def getPopularities(entities : Seq[URI]) : Seq[Float] = {
    val popArray = new Array[Float](entities.length)
    try {
      val resultsSet = queriesFor(entities).map { _.execSelect }

      for(results <- resultsSet ; sol <- results ; variable <- sol.varNames) 
        popArray(variable.toInt) = sol.getLiteral(variable).getFloat
    } catch {
      case e : Exception =>
        log.error(e.toString)
        log.error("Error while retrieving popularities. All popularities are set to 1")
        for(i <- 0 to popArray.length-1)
          popArray(i) = 1f
    }
    popArray
  }
}
