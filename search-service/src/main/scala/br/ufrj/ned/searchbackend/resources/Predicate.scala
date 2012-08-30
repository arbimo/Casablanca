package br.ufrj.ned.searchbackend.resources

class Predicate(uri:String) extends URI(uri) {

  /**
   * This key represent the variable name to store partial results in the SPARQL
   * query. 
   */
  val key = defaultKey

  def defaultKey = Predicate.getKey(xmlUri)
}


object Predicate {
  /**
   * A list of char alloweds in SPARQL variable names.
   */
  val allowedKeyChars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  def getKey(str:String) : String = 
    str.filter(allowedKeyChars.contains(_)).mkString
}