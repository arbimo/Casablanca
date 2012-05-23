package br.ufrj.ned.searchbackend


/** This class contains a single result from a search.
  * 
  * The result is described with its URI and its score
  */
case class SearchResult(val uri : String, 
                        val matchScore : Float, 
                        val popScore : Float
                        ) extends Ordered[SearchResult] {

  def this(uri : String, matchScore : Float) =
    this(uri, matchScore, 1f)
  
  def this(old : SearchResult, popScore : Float) =
    this(old.uri, old.matchScore, popScore)

  def score = matchScore * popScore
  override def compare(other : SearchResult) = - this.score.compare(other.score)
  override def toString() = score+" - "+matchScore+" - "+popScore+" - "+uri
  def toXML() =
    <search-result>
      <uri>{uri}</uri>
      <score>{score}</score>
      <match-score>{matchScore}</match-score>
      <popularity-score>{popScore}</popularity-score>
    </search-result>
}