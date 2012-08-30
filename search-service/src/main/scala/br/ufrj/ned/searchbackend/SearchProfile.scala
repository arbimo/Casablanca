package br.ufrj.ned.searchbackend

import scala.collection.JavaConversions._

import br.ufrj.ned.searchbackend.resources._
import br.ufrj.ned.searchbackend.searchcomponents._
import com.codahale.logula.Logging
import com.hp.hpl.jena.query._
import br.ufrj.ned.tools.XSDValidator
import collection.immutable.HashMap
import br.ufrj.ned.exceptions._


/** This class aims at storing information about a search backend
  * (ex : a DBPeida SPARQL end point)
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
   */
  def search(searchTerm : String) : Seq[SearchResult] = {
    backend.search(searchTerm, this)
  }

  override def toString : String = { 
    var ret = "Name : " + name + "\n" 
    ret += "Url : " + queryUrl + "\n"
    
    ret +=  "\nPredicates list with their weight : \n"
    for(p <- predicates.values) 
      ret += p.toString
    
    return ret
  }
  
  lazy val toXML = <profile/>/*{
    <profile>
      <name>{name}</name>
      <end-point>
        <url>{queryUrl}</url>
      </end-point>
      <search>
        {predicates.values.map(pred => pred.toXML)}
      </search>
      <popularity>
        {popularities.map(pop => pop.toXML)}
      </popularity>
      {types.map(typeSeq =>
      <type-constraint>
        {typeSeq.map(typeUri => <type>{typeUri}</type>)}
      </type-constraint>)}
      <properties>
        {properties.map(prop => prop.toXML)}
      </properties>
    </profile>
  }*/
  
  
}

/**Companion object for SearchProfile defining a constructor to
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
    try {

      /** Checks wether the XML is valid according to the provided schema */
      val schemaIn = this.getClass.getResourceAsStream("profile.xsd")
      if(schemaIn == null)
        log.error("Unable to find XML schema for profiles")
      else if(!XSDValidator.validate(config.mkString, schemaIn))
        throw new InvalidProfileException("The XML profile doesn't match the XSD")

      val name = (config\"name").text
      val queryUrl = (config\"end-point"\"url").text
      
      /* Get the predicates to use */
      var predicates = new HashMap[String, SearchPredicate]()

      for (predNode <- config\"search"\"search-predicate") {
        val predicate = SearchPredicate(predNode)
        predicates += (predicate.key -> predicate)
      }

      /* Get the popularity measurement method */
      val popMeasures = config\"popularity"\"measure"
      val popularities : Seq[SpecializationComponent] = 
        for(measureNode <- popMeasures) yield
          SimplePopularity(measureNode)

      /* get the type constraints */
      val typeUris = config\"type-constraint"\"type"
      val constraints : Seq[SpecializationComponent] = 
        for(uri <- (typeUris).map(_.text)) yield
          new TypeConstraint(new URI(uri))

      /* get the properties */
      val propertiesNodes = config\"properties"\"property"
      val properties : Seq[SpecializationComponent] = 
        for(propNode <- propertiesNodes) yield
          SimpleProperty(propNode)
      
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