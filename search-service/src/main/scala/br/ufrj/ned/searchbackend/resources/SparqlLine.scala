/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufrj.ned.searchbackend.resources

class SparqlLine(s:Resource, p:Resource, o:Resource) {

  def toSparql(candidateVar:Variable, targetVar:Variable) : String = {
    var line = ""

    line += {
      if(s == Candidate) candidateVar.toSparql
      else if(s == Target) targetVar.toSparql
      else s.toSparql
    }
    
    line += " " + {
      if(p == Candidate) candidateVar.toSparql
      else if(p == Target) targetVar.toSparql
      else p.toSparql
    }

    line += " " + {
      if(o == Candidate) candidateVar.toSparql
      else if(o == Target) targetVar.toSparql
      else o.toSparql
    }

    line + " . "
  }

  def toXML = 
    <triple>
      <s type={s.typeStr}>{s.value}</s>
      <p type={p.typeStr}>{p.value}</p>
      <o type={o.typeStr}>{o.value}</o>
    </triple>
  
}
