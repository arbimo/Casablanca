package br.ufrj.ned.searchbackend

import com.hp.hpl.jena.query.ResultSet
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
 * Use SearchBackend.normalizeURI for that.
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
	private def queryFor(entity : String) : QueryExecution = {
		val queryText = 
			"SELECT ?popularity WHERE {" +
	    entity + " " + method.predicate + " "+popVariable +
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
	private def queryFor(entities : Seq[String]) : Seq[QueryExecution] = {
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
			queriesText(current) += "OPTIONAL { "+ entities(i) +" "+ method.predicate +" ?"+i +" } . " 

			if((i+1)%maxVar == 0 || i+1 == entities.length)
				queriesText(current) += " } "	
		}	
		val queries = queriesText map { QueryFactory.create(_) }
		queries map { QueryExecutionFactory.sparqlService(endPoint, _) }
	}
	
	/**
	 * Returns the popularity associated with an entity
	 * 
	 * If no popularity is found, the popularity is set to 0
	 * 
	 * @param entity URI of the entity
	 * @return popularity associated with this URI
	 */
	def getPopularity(entity : String) : Float = {
		val qexec = new QueryExecuter

		/* Use of timer of 200ms to avoid blocking whole application on query execution */
		val returnValue = qexec !? (200, queryFor(entity))
		log.warn(entity)
		
		val popularity  =
			returnValue match {
				case Some(results:ResultSet) => {
						if(results.hasNext) {
							results.nextSolution.getLiteral(popVariable).getFloat
						} else {
							log.warn("This entity has no popularity measure : "+entity)
							0f
						}
				}
				case None => 
					log.error("Query execution timed out")
					0f
			}
		popularity
	}

	/**
	 * Returns a Seq of popularities associated with the entities passed as parameter
	 * 
	 * If no popularity is found, the popularity is set to 0
	 * 
	 * @param a Seq of URIs for the entities to search for
	 * @return a Seq of popularities with the same index as the corresponding entity
	 */
	def getPopularities(entities : Seq[String]) : Seq[Float] = {
		val resultsSet = queryFor(entities).map { _.execSelect }
		val popArray = new Array[Float](entities.length)

		for(results <- resultsSet ; sol <- results ; variable <- sol.varNames) 
			popArray(variable.toInt) = sol.getLiteral(variable).getFloat
		
		popArray
	}
}
