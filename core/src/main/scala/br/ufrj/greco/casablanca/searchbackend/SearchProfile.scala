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

import scala.collection.JavaConversions._

import br.ufrj.greco.casablanca.searchbackend.resources._
import br.ufrj.greco.casablanca.searchbackend.searchcomponents._
import com.codahale.logula.Logging
import com.hp.hpl.jena.query._
import br.ufrj.greco.casablanca.tools.XSDValidator
import collection.immutable.HashMap
import br.ufrj.greco.casablanca.exceptions._
import scala.xml.Node
import scala.xml.NodeSeq


/** This class aims at storing information about a search backend
  * (ex : a DBPedia SPARQL end point)
  * 
  * It provides ways to create queries against this backend
  */
class SearchProfile(val name : String,
                    val queryUrl : String,
                    val predicates : HashMap[String, SearchPredicate],
                    val specialization : Seq[SpecializationComponent],
                    val backend : SearchBackend
                     ) extends Logging {

  /**
   * Perform a search on the associated SearchBackend
   * 
   * @param searchTerm The word or group of words that is to be searched for.
   */
  def search(searchTerm : String) : Seq[SearchResult] = {
    backend.search(searchTerm, this)
  }

  /**
   * The list of property configurations defined with the profile.
   */
  lazy val properties : Seq[Property] =
    specialization.filter(_.isInstanceOf[Property]).map(_.asInstanceOf[Property])

  /**
   * The list of popularity configurations defined with the profile.
   */
  lazy val popularities : Seq[Popularity] =
    specialization.filter(_.isInstanceOf[Popularity]).map(_.asInstanceOf[Popularity])

  /**
   * The list of constraint configurations defined with the profile.
   */
  lazy val constraints : Seq[Constraint] =
    specialization.filter(_.isInstanceOf[Constraint]).map(_.asInstanceOf[Constraint])

  override def toString : String = { 
    var ret = "Name : " + name + "\n" 
    ret += "Url : " + queryUrl + "\n"
    
    ret +=  "\nPredicates list with their weight : \n"
    for(p <- predicates.values) 
      ret += p.toString
    
    return ret
  }
  
  lazy val toXML = 
    <profile>
      <name>{name}</name>
      <end-point>
        <url>{queryUrl}</url>
      </end-point>
      <search>
        {predicates.values.map(pred => pred.toXML)}
      </search>
      <popularities>
        {popularities.map(_.toXML)}
      </popularities>
      <constraints>
        {constraints.map(_.toXML)}
      </constraints>
      <properties>
        {properties.map(_.toXML)}
      </properties>
    </profile>
  
}

/**
 * Companion object for SearchProfile defining a constructor to
 * parse an XML config file.
 *
 * Usage : `val sb = SearchProfile(scala.xml.XML.loadFile(configFile))`
 */
object SearchProfile extends Logging {
  
  /**
   * Constructor for SearchProfile
   *
   * @param configFile a file path containing the XML configuration
   * @return a SearchProfile built from the configuration
   */
  def apply(configFile : String) : Option[SearchProfile] =
    SearchProfile(scala.xml.XML.loadFile(configFile))

  /**Constructor for SearchProfile
   *
   * @param config an xml node containing the configuration
   * @return a SearchProfile built from the configuration
   */
  def apply(config : scala.xml.Node) : Option[SearchProfile] = {
    /** Returns all direct <full/> or <light/> children */
    def lightFullChildren(node : xml.NodeSeq) = 
      if(node.nonEmpty)
        node.map(_.child)
            .fold(NodeSeq.Empty)(_ union _)
            .filter(child => child.label=="light" || child.label=="full")
      else
        NodeSeq.Empty

    try {

      /** Checks wether the XML is valid according to the provided schema 
      val schemaIn = this.getClass.getResourceAsStream("profile.xsd")
      if(schemaIn == null)
        log.error("Unable to find XML schema for profiles")
      else if(!XSDValidator.validate(config.mkString, schemaIn))
        throw new InvalidProfileException("The XML profile doesn't match the XSD")
      */

      val name = (config\"name").text
      val queryUrl = (config\"end-point"\"url").text
      
      /* Get the predicates to use */
      var predicates = new HashMap[String, SearchPredicate]()

      for (predNode <- config\"search"\"search-predicate") {
        val predicate = SearchPredicate(predNode)
        predicates += (predicate.key -> predicate)
      }

      /* Get the popularity measurement method */
      val popMeasures = lightFullChildren(config\"popularities")
      val popularities : Seq[SpecializationComponent] = 
        for(measureNode <- popMeasures)
          yield Popularity(measureNode)

      /* get the constraints */
      val consNodes = lightFullChildren(config\"constraints")
      val constraints : Seq[SpecializationComponent] = 
        for(cons <- consNodes)
          yield Constraint(cons)

      /* get the properties */
      val props= lightFullChildren(config\"properties")
      val properties : Seq[SpecializationComponent] = 
        for(propNode <- props)
            yield Property(propNode)
      
      Some(new SearchProfile(name,
                             queryUrl,
                             predicates,
                             popularities ++:
                             constraints ++:
                             properties,
                             SearchBackend.getDefault))
    } catch {
      case e => 
        log.error("Unable to read XML : %s", e)
        None
    }
  }

}