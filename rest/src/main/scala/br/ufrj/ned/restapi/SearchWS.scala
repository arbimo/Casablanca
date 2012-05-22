package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._

@Path("/search/{searchTerm}")
class SearchWS {
  @GET @Produces(Array("text/xml"))
  def doGet(@PathParam("searchTerm") searchTerm:String) = {
		val config = "/home/arthur/Info/ScalaJena/search-service/src/main/resources/local-yago.xml"
		val sb = SearchBackend(scala.xml.XML.loadFile(config))
		val resultSeq = sb.search(searchTerm)
		(<search term={searchTerm}>{resultSeq.map(res => res.toXML)}</search>).toString
	}
}