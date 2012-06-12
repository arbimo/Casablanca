/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufrj.ned.searchbackend

case class PopularityMethod(val predicate : URI) {

  def this(pred : String) = this(new URI(pred))
  
  override def toString : String = "Measurement predicate : "+predicate

  def toXML = <measure><predicate>{predicate}</predicate></measure>
}

object PopularityMethod {

  /**
   * Takes <measure> node and returns the corresponding PopularityMethod
   * instance.
   */
  def apply(measureNode : scala.xml.Node) : PopularityMethod = {
    val pred = (measureNode\"predicate").text
    new PopularityMethod(new URI(pred))
  }
}
