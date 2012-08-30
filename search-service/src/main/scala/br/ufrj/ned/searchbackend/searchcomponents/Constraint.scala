package br.ufrj.ned.searchbackend.searchcomponents

import br.ufrj.ned.searchbackend._
import br.ufrj.ned.searchbackend.resources._


class Constraint extends SpecializationComponent {

  val result = None
}

class TypeConstraint(myType:URI) extends Constraint {

  addLine(Candidate, new URI("rdf:type"), myType)
  
}