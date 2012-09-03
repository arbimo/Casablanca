package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._

trait SpecializationComponent extends SearchComponent {

  def toSparql(candidate:Variable):String = {
    var str = 
      if(optional) 
        " OPTIONAL { "
      else
        " { "
      
    for(l <- lines)
      result match {
        case Some(resVar) =>
          str += l.toSparql(candidate, resVar)
        case None =>
          str += l.toSparql(candidate, new Variable(""))
      }
    str + " } "
  }
  
}

