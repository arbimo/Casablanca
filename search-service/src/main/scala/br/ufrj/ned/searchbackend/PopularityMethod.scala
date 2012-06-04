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

