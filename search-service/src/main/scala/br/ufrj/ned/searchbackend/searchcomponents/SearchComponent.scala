package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._
import scala.collection.mutable.ArrayBuffer

trait SearchComponent {

  val result : Option[Variable]
  val key = SearchComponent.getFreeID
  val optional = false
  var lines = new ArrayBuffer[SparqlLine]()

  def addLine(s:Resource, p:Resource, o:Resource) {
    val line = new SparqlLine(s, p, o)
    lines += line
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




