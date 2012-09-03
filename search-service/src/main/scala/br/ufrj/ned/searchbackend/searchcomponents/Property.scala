package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._



class Property extends SpecializationComponent {

  val result = Some(new Variable(key))
  override val optional = true
  var label = "property"

  def toXML =
    <full>
      <label>{label}</label>
      {for(l <- lines) yield l.toXML}
    </full>
}

object Property {

  def apply(node : scala.xml.Node) : Property = {
    if(node.label == "light")
      PropertyLight(node)
    else {
      println(node)
      val prop = new Property
      prop.label = (node\"label").text
      for(triple <- node\"triple") {
        val s = Resource((triple\"s").head)
        val p = Resource((triple\"p").head)
        val o = Resource((triple\"o").head)
        prop.addLine(s, p, o)
      }
      prop
    }
  }
}

class PropertyLight(predicate:Predicate, localLabel:String) extends Property {

  label = localLabel
  addLine(Candidate, predicate, Target)

  override def toXML = 
    <light>
      <label>{label}</label>
      <predicate>{predicate.value}</predicate>
    </light>
}

object PropertyLight {

  def apply(node:scala.xml.Node) : Property = {
    val label = (node\"label").text
    val pred = (node\"predicate").text
    new PropertyLight(new Predicate(pred), label)
  }
}