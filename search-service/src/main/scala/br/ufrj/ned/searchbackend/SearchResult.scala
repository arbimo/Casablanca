package br.ufrj.ned.searchbackend


/** This class contains a single result from a search.
  * 
  * The result is described with its URI and its score
  */
case class SearchResult(val uri : URI, val scores : List[Score]
                        ) extends Ordered[SearchResult] {

  def addScore(score : Score) = new SearchResult(uri, score :: scores)
  def score = scores.foldLeft(1f)({_ * _.normalizedValue})
  
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