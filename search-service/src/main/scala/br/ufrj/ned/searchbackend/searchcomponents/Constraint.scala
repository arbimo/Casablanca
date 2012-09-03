package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._


class Constraint extends SpecializationComponent {

  val result = None

  def toXML =
    <full>
      {for(l <- lines) yield l.toXML}
    </full>
}

object Constraint {

  def apply(node : xml.Node) : Constraint = {
    if(node.label == "light")
      ConstraintLight(node)
    else {
      val c = new Constraint
      for(triple <- node\"triple") {
        val s = Resource((triple\"s").head)
        val p = Resource((triple\"p").head)
        val o = Resource((triple\"o").head)
        c.addLine(s, p, o)
      }
      c
    }
  }
}

class ConstraintLight(myType:URI) extends Constraint {

  addLine(Candidate, new URI("rdf:type"), myType)

  override def toXML =
    <light>
      <type>{myType}</type>
    </light>
  
}

object ConstraintLight {

  def apply(node : xml.Node) : Constraint = {
    new ConstraintLight(new URI((node\"type").text))
  }
}