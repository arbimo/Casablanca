package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._



class Property extends SpecializationComponent {

  val result = Some(new Var(key))
  override val optional = true
  var label = "property"

}

class SimpleProperty(predicate:Predicate, localLabel:String) extends Property {

  label = localLabel
  addLine(Candidate, predicate, result.get)
}

object SimpleProperty {

  def apply(node:scala.xml.Node) : SimpleProperty = {
    val label = (node\"label").text
    val pred = (node\"predicate").text
    new SimpleProperty(new Predicate(pred), label)
  }
}