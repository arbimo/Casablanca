package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._

trait SpecializationComponent extends SearchComponent {

  def toSparql(candidate:Var):String = {
    var str = 
      if(optional) 
        " OPTIONAL { "
      else
        " { "
      
    for(l <- lines) {
      str += l.toSparql(candidate)
    }
    str + " } "
  }
  
}

