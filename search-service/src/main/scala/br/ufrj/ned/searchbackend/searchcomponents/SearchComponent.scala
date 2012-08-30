package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._

trait SearchComponent {

  val result : Option[Var]
  val key = SearchComponent.getFreeID
  val optional = false
  var lines : Seq[SparqlLine] = Nil

  def addLine(s:Resource, p:Resource, o:Resource) {
    val line = new SparqlLine(s, p, o)
    lines = line +: lines
  }
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




