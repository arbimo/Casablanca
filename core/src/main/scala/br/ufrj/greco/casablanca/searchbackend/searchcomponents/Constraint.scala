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