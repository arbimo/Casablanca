package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._


class Popularity extends SpecializationComponent {

  val result = Some(new Var(key))
  override val optional = true
  var label = "popularity"

}

class SimplePopularity(pred:Predicate, popLabel:String) extends Popularity {
  addLine(Candidate, pred, result.get)
  label = popLabel

}

object SimplePopularity {
  /**
   * Takes <measure> node and returns the corresponding PopularityMethod
   * instance.
   */
  def apply(measureNode : scala.xml.Node) : SimplePopularity= {
    val pred = (measureNode\"predicate").text
    val label =
      if((measureNode\"label").isEmpty)
        "popularity"
      else
        (measureNode\"label").text

    new SimplePopularity(new Predicate(pred), label)
  }
}