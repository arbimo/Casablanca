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
 
package br.ufrj.greco.casablanca.searchbackend.resources

/**
 * This trait is the ancestor of the different types that might appear in
 * a SPARQL query
 */
trait Resource {

  /**
   * how a resource should appear in a SPARQL query
   */
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
