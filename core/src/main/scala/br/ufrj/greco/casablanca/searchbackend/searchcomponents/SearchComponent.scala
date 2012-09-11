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
import scala.collection.mutable.ArrayBuffer

trait SearchComponent {

  /**
   * Contains the variable that would store the result after their
   * treatment (i.e. after the application of a COUNT ...)
   * 
   * None if no results are expected (this is the case for Constraints
   */
  val result : Option[Variable]
  
  /**
   * Contains the variable that would the temporary results (i.e. the one
   * that would be used as a base for treatment such as COUNT or AVG)
   * 
   * None if no such variable is required.
   */
  val target : Option[Variable]

  val key = SearchComponent.getFreeID
  val optional = false
  var lines = new ArrayBuffer[SparqlLine]()

  def addLine(s:Resource, p:Resource, o:Resource) {
    val line = new SparqlLine(s, p, o)
    lines += line
  }
  
  /**
   * Should this appear in the GROUP BY clause
   */
  val groupBy : Boolean
  
  /**
   * How should this appear in the select clause.
   * Empty String if no result is to be retrieved
   */
  val selectString : String
}

object SearchComponent {

  private var IDCount = 0

  def getFreeID : String = {
    this.synchronized {
      IDCount += 1
      IDCount.toString
    }
  }
  
}




