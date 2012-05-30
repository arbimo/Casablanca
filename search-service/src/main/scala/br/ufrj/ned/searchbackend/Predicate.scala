package br.ufrj.ned.searchbackend


/** 
 * This class stores the uri of a predicate
 * and the its associated weight (i.e. how important
 * is this predicate to know which candidate matches the best)
 */
case class Predicate(uri : String, weight : Float) {
  private final val chars = ('a' to 'z') ++ ('A' to 'Z') ++ ('0' to '9')

  val key =
    uri.foldLeft("")( (key, currentChar) => if(chars.contains(currentChar)) key + currentChar else key )

  def toXML = 
    <search-predicate uri={uri} weight={weight.toString}/>
}