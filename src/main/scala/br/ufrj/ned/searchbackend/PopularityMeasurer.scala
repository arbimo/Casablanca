package br.ufrj.ned.searchbackend

import scala.collection.JavaConversions._
import com.hp.hpl.jena.query.QueryExecution
import com.hp.hpl.jena.query.QueryExecutionFactory
import com.hp.hpl.jena.query.QueryFactory

class PopularityMeasurer(endPoint : String, method : PopularityMethod) {

	private val popVariable = "?popularity"

	private def queryFor(entity : String) : QueryExecution = {
		val queryText = 
			"SELECT ?popularity WHERE {" +
	    entity + " " + method.predicate + " "+popVariable +
			" } "

		println(queryText)
		val query = QueryFactory.create(queryText)
			
		QueryExecutionFactory.sparqlService(endPoint, query)
	}
	
	def getPopularity(entity : String) : Float = {
		val results = queryFor(entity).execSelect

		val popularity = results.nextSolution.getLiteral(popVariable).getFloat

		/* There should be only one popularity value */
		assert(!results.hasNext)
		
		popularity
	}
}
