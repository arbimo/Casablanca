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
case class Match(val method : String, val uri : String) {

  override def toString = 
    "Match method : " + method + " - URI : " + uri
  
  def toXML = 
    <match>
      <type>{method}</type>
      {if(method=="contains")
        <contains-uri>{uri}</contains-uri>}
    </match>
}
