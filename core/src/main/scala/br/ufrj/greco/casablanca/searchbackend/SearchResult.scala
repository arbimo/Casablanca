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

import br.ufrj.greco.casablanca.searchbackend.resources._

/** This class contains a single result from a search.
  * 
  * The result is described with its URI and its score
  */
case class SearchResult(val uri : URI, 
                        val scores : Seq[Score],
                        val properties :  Map[String, Set[String]]
                        ) extends Ordered[SearchResult] {

  def this(uri:String, scores:Seq[Score], prop:Map[String,Set[String]]) = 
    this(new URI(uri), scores, prop)

  def this(uri:String, scores:Seq[Score]) = this(uri, scores, Map[String,Set[String]]()) 
  
  /**
   * The (final) score is defined as the average of the normalized scores
   */
  def score = scores.foldLeft(0f)({_ + _.normalizedValue}) / scores.length
  
  /**
   * Returns :
   *  x < 0 when this.score > other.score
   *  x == 0 when this.score == other.score
   *  x > 0 when this.score > other.score
   */
  override def compare(other : SearchResult) = - this.score.compare(other.score)
  
  override def toString() = score+"\t"+uri+"\t"+ 
      (for(s<-scores) yield s.toString).foldLeft("")({_+" - "+_})
  
  def toXML() =
    <search-result>
      <uri>{uri}</uri>
      <global-score>{score}</global-score>
      <scores>{for(s <- scores) yield s.toXML}</scores>
      <properties>
        {for(label <- properties.keySet) yield
           <property>
             <name>{label}</name>
             {for(value <- properties(label)) yield
             <value>{value}</value>}
           </property>
         }
      </properties>
    </search-result>
}
