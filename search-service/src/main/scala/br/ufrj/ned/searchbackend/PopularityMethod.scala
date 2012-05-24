/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufrj.ned.searchbackend

case class PopularityMethod(val predicate : String, val max : Float) {
  override def toString : String = "Measurement predicate : "+predicate+" - Max value : "+max  
}

