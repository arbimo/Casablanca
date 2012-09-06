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