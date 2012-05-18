package br.ufrj.ned.searchbackend


/** This class simply includes a description of the search method
 * to use in the backend.
 * 
 * method can be :
 *
 *  - exact : `?result <search-uri> "search-text"`
 *
 *  - contains : `?result <search-uri> ?text . ?text <match.uri> "search-text"`
 */
case class Match(val method : String, val uri : String)
