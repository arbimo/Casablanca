/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package br.ufrj.ned.searchbackend

import scala.collection.mutable.HashMap

class SearchResultSet(methods:Set[PopularityMethod], properties:Set[PropertyPredicate]) {

  /**
   * Collection of PopularityMethod index by their key
   */
  val popMethods = new HashMap[String, PopularityMethod]
  for(meth <- methods)
    popMethods += (meth.key -> meth)

  /**
   * Collection of PropertyPredicate index by their key
   */
  val props = new HashMap[String, PropertyPredicate]
  for(p <- properties)
    props += (p.key -> p)

  private val scoreKeys = (methods map {_.key}) + "match"

  private val normalizationFunctions = new HashMap[String, Float=>Float]
	
  /**
   * Double indexed data structure used to link every candidate URI to 
   * a set of scores.
   * 
   * First key is the URI itself, second key is the score measurement key
   */
  private val candidates = new HashMap[String, HashMap[String, Float]]

  /**
   * Double indexed data structure used to link every candidate URI to 
   * its properties.
   * 
   * First key is the URI itself, second key is the label of the corresponding 
   * PropertyPredicate.
   */
  private val candProps = new HashMap[String, HashMap[String, Set[String]]]

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

  def addProp(uri:String, propKey:String, value:String) {
    if(!candProps.contains(uri))
      candProps += (uri -> new HashMap[String,Set[String]])

    val key = props(propKey).label
    candProps(uri) += (key -> (getProp(uri, propKey) + value))
  }

  def getProp(uri:String, propKey:String) : Set[String] = {
    if(candProps.contains(uri))
      candProps(uri).getOrElse(props(propKey).label, Set[String]())
    else 
      Set[String]()
    
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
      val propMap = candProps.getOrElse(uri, HashMap[String, Set[String]]()).toMap
      new SearchResult(uri, scores, propMap)
    }
  }

  def toOrderedSearchResults : Seq[SearchResult] =
    util.Sorting.stableSort(toSearchResults)
  
}
