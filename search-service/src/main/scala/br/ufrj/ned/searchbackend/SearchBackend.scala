package br.ufrj.ned.searchbackend

trait SearchBackend {

  /**
   * Defines prefixes to be used in SPARQL queries
   */
  val prefix = 
        "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n"  + 
        "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n" +
        "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n" +
        "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"

  /** Limit the size of the results.
   *  This should be avoided not to miss pretinent results
   */
  val limit = " "

  /**
   * This function is an all in one to search for a term according to the 
   * associated SearchProfile.
   * It reutrns a sequence of results, ordered acording to their scores.
   */
  def search(searchTerm:String, profile:SearchProfile) : Seq[SearchResult]
}

object SearchBackend {

  /**
   * Returns a default SearchBackend that is supposed to be compatible 
   * with every End Point.
   */
  def getDefault : SearchBackend = {
    new BasicSparqlBackend
  }
}
