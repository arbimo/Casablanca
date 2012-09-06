/**
 * Copyright 2012 Arthur Bit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package br.ufrj.greco.casablanca.searchbackend.resources

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
