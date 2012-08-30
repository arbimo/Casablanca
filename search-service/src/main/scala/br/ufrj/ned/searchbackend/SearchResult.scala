package br.ufrj.ned.searchbackend

import br.ufrj.ned.searchbackend.resources._

/** This class contains a single result from a search.
  * 
  * The result is described with its URI and its score
  */
case class SearchResult(val uri : URI, 
                        val scores : Seq[Score],
                        val properties :  Map[String, Set[String]]
                        ) extends Ordered[SearchResult] {

  def this(uri:String, scores:Seq[Score], prop:Map[String,Set[String]]) = 
    this(new URI(uri), scores, prop)

  def this(uri:String, scores:Seq[Score]) = this(uri, scores, Map[String,Set[String]]()) 
  
  def score = scores.foldLeft(0f)({_ + _.normalizedValue}) / scores.length
  
  override def compare(other : SearchResult) = - this.score.compare(other.score)
  
  override def toString() = score+"\t"+uri+"\t"+ 
      (for(s<-scores) yield s.toString).foldLeft("")({_+" - "+_})
  
  def toXML() =
    <search-result>
      <uri>{uri}</uri>
      <global-score>{score}</global-score>
      <scores>{for(s <- scores) yield s.toXML}</scores>
      <properties>
        {for(label <- properties.keySet) yield
           <property>
             <name>{label}</name>
             {for(value <- properties(label)) yield
             <value>{value}</value>}
           </property>
         }
      </properties>
        
    </search-result>
}
