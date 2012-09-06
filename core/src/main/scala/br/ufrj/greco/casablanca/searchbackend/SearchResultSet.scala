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
 
package br.ufrj.greco.casablanca.searchbackend

import br.ufrj.greco.casablanca.searchbackend.resources._
import br.ufrj.greco.casablanca.searchbackend.searchcomponents._
import scala.collection.mutable.HashMap

/**
 * This class makes the transition between an the result of a SPARQL query
 * and and organized list of SearchResult (Seq[SearchResult]).
 * 
 * Usage : a SearchBackend should use this class to add the candidates, 
 * their score and their properties with the adapted methods of this class.
 * 
 * Once every result is added, the final results can be retrieved using the
 * toSearchResults and toOrderedSearchResult methods. 
 * When calling one of those methods, the normalized score of each entity will
 * be computed.
 */
class SearchResultSet(methods:Set[Popularity], properties:Set[Property]) {

  /**
   * Collection of PopularityMethod index by their key
   */
  val popMethods = new HashMap[String, Popularity]
  for(meth <- methods)
    popMethods += (meth.key -> meth)

  /**
   * Collection of PropertyPredicate index by their key
   */
  val props = new HashMap[String, Property]
  for(p <- properties)
    props += (p.key -> p)

  private val scoreKeys = (methods map {_.key}) + "match"
  
  /**
   * Maps of each popularity method to the corresponding nomarlization function.
   */
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

  /** 
   * Add a score to a candidate.
   * If a score is already present, it will be overwritten.
   * 
   * @param uri URI of the candidate
   * @param scoreKey key of the Popularity instance, "match" for a match score
   * @param value the value to be added
   */
  def setScore(uri:String, scoreKey:String, value:Float) {
    if(!candidates.contains(uri))
      candidates += (uri -> new HashMap[String,Float])

    candidates(uri) += (scoreKey -> value)
  }

  /** 
   * Retrieves a score of a candidate.
   * 
   * @param uri URI of the candidate
   * @param scoreKey key of the Popularity instance, "match" for a match score
   */
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

  /**
   * Computes all the functions that are to be used to normalized the scores.
   * 
   * Since each function depends on the scores present in the set, this function
   * should only be called when the scores are fully populated.
   * 
   * The normalization functions are stored in the normalizationFunctions attribute.
   */
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
