package br.ufrj.ned.searchbackend


/** This class contains a single result from a search.
  * 
  * The result is described with its URI and its score
  */
case class SearchResult(val uri : URI, val scores : Seq[Score]
                        ) extends Ordered[SearchResult] {

  def this(uri:String, scores:Seq[Score]) = this(new URI(uri), scores)
  
  def score = scores.foldLeft(0f)({_ + _.normalizedValue}) / scores.length
  
  override def compare(other : SearchResult) = - this.score.compare(other.score)
  
  override def toString() = score+"\t"+uri+"\t"+ 
      (for(s<-scores) yield s.toString).foldLeft("")({_+" - "+_})
  
  def toXML() =
    <search-result>
      <uri>{uri}</uri>
      <global-score>{score}</global-score>
      <scores>{for(s <- scores) yield s.toXML}</scores>
    </search-result>
}
