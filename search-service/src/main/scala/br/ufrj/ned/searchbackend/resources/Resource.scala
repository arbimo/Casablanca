package br.ufrj.ned.searchbackend.resources

trait Resource {

  def toSparql : String 

  val typeStr : String
  val value : String
}

object Resource {

  def apply(node : scala.xml.Node) : Resource = {
    (node\"@type").text match {
      case "Candidate" => Candidate
      case "Literal" => new Literal(node.text)
      case "URI" => new URI(node.text)
      case "Predicate" => new Predicate(node.text)
      case "Variable" => new Variable(node.text)
      case "Target" => Target
    }
  }
  
}

class Literal(valueStr : String) extends Resource {

  def toSparql = ""

  val typeStr = "Literal"
  val value = valueStr

}

class Variable(varName:String) extends Resource {

  def toSparql = "?"+varName
  
  val typeStr = "Variable"
  val value = varName
}

object Candidate extends Resource {
  def toSparql = ""
  val typeStr = "Candidate"
  val value = ""
}

object Target extends Resource {
  def toSparql = ""
  val typeStr = "Target"
  val value = ""
}
