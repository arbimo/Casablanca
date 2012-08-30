/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufrj.ned.searchbackend.resources

class SparqlLine(s:Resource, p:Resource, o:Resource) {

  def toSparql(candidateVar:Var) : String = {
    var line = ""

    line += {
      if(s == Candidate)
        candidateVar.toSparql
      else
        s.toSparql
    }

    line += {
      if(p == Candidate)
        candidateVar.toSparql
      else
        p.toSparql
    }

    line += {
      if(o == Candidate)
        candidateVar.toSparql
      else
        o.toSparql
    }

    line
  }
  
}
