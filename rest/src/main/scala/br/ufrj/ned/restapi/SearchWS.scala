package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.backendmanager._

@Path("/search/{searchTerm}")
class SearchWS {
  @GET @Produces(Array("text/xml"))
  def doGet(@PathParam("searchTerm") searchTerm:String) = {
    val backendOption = BackendManager !? RetrieveDefault
    val sb = backendOption match {case sb:SearchBackend => sb }
    val resultSeq = sb.search(searchTerm)
    (<search term={searchTerm}>{resultSeq.map(res => res.toXML)}</search>).toString
  }
}