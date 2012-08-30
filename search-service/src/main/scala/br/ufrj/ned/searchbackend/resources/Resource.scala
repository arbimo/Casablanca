package br.ufrj.ned.searchbackend.resources

trait Resource {

  def toSparql : String 
}

class Literal extends Resource {

  def toSparql = ""

}

class Var(varName:String) extends Resource {

  def toSparql = "?"+varName
  
}

object Candidate extends Resource {
  def toSparql = ""
}
