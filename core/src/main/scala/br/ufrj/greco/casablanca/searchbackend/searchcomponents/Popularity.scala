/**
 * Copyright 2012 Arthur Bit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package br.ufrj.greco.casablanca.searchbackend.searchcomponents

import br.ufrj.greco.casablanca.searchbackend._
import br.ufrj.greco.casablanca.searchbackend.resources._


class Popularity(treatmentID:String) extends SpecializationComponent {
  def this() { this("Min") }
  
  val result = Some(new Variable(key))
  val target = treatmentID match {
    case "No" => result //target and result identical
    case _    => Some(new Variable(SearchComponent.getFreeID))
  }
  
  private val treatment = ResultTreatment(result.get, target.get, treatmentID)
  
  val selectString = treatment.selectString
  val groupBy = treatment.groupBy
  
  override val optional = true
  var label = "popularity"

  def toXML =
    <full>
      <label>{label}</label>
      <treatment>{treatment.ID}</treatment>
      {for(l <- lines) yield l.toXML}
    </full>
}

object Popularity {

  def apply(node : xml.Node) : Popularity = {
    if(node.label == "light")
      PopularityLight(node)
    else {
      val pop =
        if(node\"treatment" isEmpty)
          new Popularity
        else
          new Popularity((node\"treatment").text)
        
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