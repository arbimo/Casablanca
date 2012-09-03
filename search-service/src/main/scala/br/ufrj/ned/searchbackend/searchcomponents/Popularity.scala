package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._


class Popularity extends SpecializationComponent {

  val result = Some(new Variable(key))
  override val optional = true
  var label = "popularity"

  def toXML =
    <full>
      <label>{label}</label>
      {for(l <- lines) yield l.toXML}
    </full>
}

object Popularity {

  def apply(node : xml.Node) : Popularity = {
    if(node.label == "light")
      PopularityLight(node)
    else {
      val pop = new Popularity
      pop.label = 
        if(node\"label" isEmpty)
          "popularity"
        else 
          (node\"label").text

      for(triple <- node\"triple") {
        val s = Resource((triple\"s").head)
        val p = Resource((triple\"p").head)
        val o = Resource((triple\"o").head)
        pop.addLine(s, p, o)
      }
      pop
    }
  }
}

class PopularityLight(pred:Predicate, popLabel:String) extends Popularity {
  addLine(Candidate, pred, Target)
  label = popLabel

  override def toXML =
    <light>
      <label>{label}</label>
      <predicate>{pred}</predicate>
    </light>

}

object PopularityLight {
  /**
   * Takes <light> node and returns the corresponding Popularity
   * instance.
   */
  def apply(lightNode : scala.xml.Node) : PopularityLight = {
    val pred = (lightNode\"predicate").text
    val label =
      if((lightNode\"label").isEmpty)
        "popularity"
      else
        (lightNode\"label").text

    new PopularityLight(new Predicate(pred), label)
  }
}