package br.ufrj.ner.SearchBackend


import scala.collection.mutable.ArrayBuffer


/** This class stores the uri of a predicate 
  * and the its associated weight (i.e. how important
  * is this predicate to know which candidate matches the best)
  */
case class Predicate(uri : String, weight : Int)



/** This class aims at storing information about a search backend
  * (ex : a DBPeida SPARQL end point)
  * 
  * It provides ways to create queries against this backend
  */
class SearchBackend {

  var name = ""
  
  var url = ""
  
  var predicates = new ArrayBuffer[Predicate](0)
  
  override def toString : String = { 
    var ret = "Name : " + name + "\n" 
    ret += "Url : " + url + "\n"
    
    ret +=  "\nPredicates list with their weight : \n"
    for(p <- predicates) 
      ret += p.uri + " - " + p.weight + "\n"
    
    return ret
  }

}