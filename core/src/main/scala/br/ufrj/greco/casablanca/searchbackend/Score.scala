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
 
package br.ufrj.greco.casablanca.searchbackend

/**
 * This class consists of a single score value associated with a search result.
 * 
 * It consists of the label associated with the popularity retrievment method,
 * the measured raw value and a normalized value.
 */
class Score(val label:String, val rawValue:Float, val normalizedValue:Float) {

  def this(label:String, value:Float) =
    this(label, value, value)

  override def toString = label+":"+normalizedValue

  def toXML = 
    <score> 
      <label>{label}</label>
      <value>{normalizedValue}</value>
    </score>

}

object Score {

  /**
   * Used to determine the normalization function corresponding to a set of scores.
   * 
   * Given a set of scores (represented as float), this method creates a 
   * pure-functions that, given a raw score, computes the associated normalized 
   * score.
   * 
   * The produced function linearly bring the scores in the [1..9] interval.
   */
  def normalizationFunction(scores:Seq[Float]) : (Float => Float) = {
    var min = Float.MaxValue
    var max = Float.MinValue
    for(s <- scores) {
      if(s < min)
        min = s
      if(s > max)
        max = s
    }

    assert(scores.isEmpty || min>=0f, "Minimum value is biger than 0")

    return s => if(max == min) 5f
                else (s-min)*8f / (max-min)  +1f
  }
}
