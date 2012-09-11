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

import br.ufrj.greco.casablanca.searchbackend.resources._
import com.codahale.logula.Logging

abstract class ResultTreatment(val resultVar:Variable, val targetVar:Variable) {
  
  val ID : String
  def selectString : String
  def groupBy : Boolean
  
}

object ResultTreatment extends Logging {
  
  def apply(resultVar:Variable, targetVar:Variable, method:String) : ResultTreatment = {
    method match {
      case "No" => new NoTreatment(resultVar,targetVar)
      case "Count" => new CountTreatment(resultVar, targetVar)
      case "Min" => new MinTreatment(resultVar, targetVar)
      case "Max" => new MaxTreatment(resultVar, targetVar)
//    case "Avg" => new AvgTreatment(resultVar, targetVar)
      case _ => {
          log.error("No implementation for treatment method "+method )
          throw new Exception("Unable to find treatment method : "+method)
      }

    }
  }
}

class NoTreatment(resultVar:Variable, targetVar:Variable)
  extends ResultTreatment(resultVar, targetVar) {
    
  val ID = "No"
  def selectString = resultVar.toSparql
  def groupBy = true
  
}

class CountTreatment(resultVar:Variable, targetVar:Variable)
  extends ResultTreatment(resultVar, targetVar) {

  val ID = "Count"
  def selectString = " (COUNT("+targetVar.toSparql+") AS "+resultVar.toSparql+") "
  def groupBy = false
}

class MinTreatment(resultVar:Variable, targetVar:Variable)
  extends ResultTreatment(resultVar, targetVar) {

  val ID = "Min"
  def selectString = " (MIN("+targetVar.toSparql+") AS "+resultVar.toSparql+") "
  def groupBy = false
}
/**
 * Not widely implemented (especialy not in ARQ),
 * disabling to avoid misleading results.
 * Implementation might be done inside SearchResultSet
 * 
class AvgTreatment(resultVar:Variable, targetVar:Variable)
  extends ResultTreatment(resultVar, targetVar) {

  val ID = "Avg"
  def selectString = " (AVG("+targetVar.toSparql+") AS "+resultVar.toSparql+") "
  def groupBy = false
}
*/

class MaxTreatment(resultVar:Variable, targetVar:Variable)
  extends ResultTreatment(resultVar, targetVar) {

  val ID = "Max"
  def selectString = " (MAX("+targetVar.toSparql+") AS "+resultVar.toSparql+") "
  def groupBy = false
}