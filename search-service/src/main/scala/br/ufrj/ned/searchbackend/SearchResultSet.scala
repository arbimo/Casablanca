/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufrj.ned.searchbackend

import scala.collection.mutable.HashMap

class SearchResultSet(methods:Set[PopularityMethod]) {

  val popMethods = new HashMap[String, PopularityMethod]
  for(meth <- methods)
    popMethods += (meth.key -> meth)

  private val scoreKeys = (methods map {_.key}) + "match"

  private val normalizationFunctions = new HashMap[String, Float=>Float]
	
  /**
   * Double indexed data structure used to link every candidate URI to 
   * a set of scores.
   * 
   * First key is the URI itself, second key is the score measurement key
   */
  private val candidates = new HashMap[String, HashMap[String, Float]]

  def setScore(uri:String, scoreKey:String, value:Float) {
    if(!candidates.contains(uri))
      candidates += (uri -> new HashMap[String,Float])

    candidates(uri) += (scoreKey -> value)
  }

  def getScore(uri:String, scoreKey:String) = {
    if(candidates.contains(uri))
      candidates(uri).getOrElse(scoreKey, 0f)
    else
      0f
  }

  def computeNormalizationFunctions {
    for(key <- scoreKeys) {
      val normFunc = Score.normalizationFunction(
        for(uri <-candidates.keySet.toSeq) yield getScore(uri, key))
      normalizationFunctions += (key -> normFunc)
    }
  }

  def toSearchResults : Seq[SearchResult] = {
    computeNormalizationFunctions
    for(uri <- candidates.keySet.toSeq ; if(URI.isValid(uri))) yield {
      val scores = for(scoreKey <- scoreKeys.toSeq) yield {
        val label = 
          if(popMethods.contains(scoreKey)) popMethods(scoreKey).label
          else "match"
        val rawScore = getScore(uri,scoreKey)
        new Score(label, rawScore, normalizationFunctions(scoreKey)(rawScore))
      }
      new SearchResult(uri, scores)
    }
  }

  def toOrderedSearchResults : Seq[SearchResult] = {
      util.Sorting.stableSort(toSearchResults)
  }
}
