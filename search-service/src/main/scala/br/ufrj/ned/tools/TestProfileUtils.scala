package br.ufrj.ned.tools

import br.ufrj.ned.searchbackend.SearchProfile
import org.xml.sax.InputSource

/**
 * This singleton object is provides a few usefull methods and attributes
 * usefull for testing. They are not supposed to be used in production since
 * they use assertions to make sure everything goes well.
 */
object TestProfileUtils {

  /**
   * A list of profiles stored as resources.
   * Those are supposed to be valid every time.
   */
  val profiles = List("/profiles/dbpedia.xml", 
                      "/profiles/dbpedia-cities.xml",
                      "/profiles/default.xml")

  /**
   * Return an XML representation of the resource corresponding to 
   * the given URI.
   * 
   * *Warning* : if the URI is not valid, this will fail on assertion.
   */
  def resourceAsXML(URI:String) = {
    val configIn = this.getClass.getResourceAsStream(URI)
    assert(configIn != null, "Profile resource not found")
    scala.xml.XML.load(new InputSource(configIn))
  }

  /**
   * Return a SearchProfile representation of the resource corresponding to 
   * the given URI.
   * 
   * *Warning* : if the URI or XML config is not valid, 
   * this will fail on assertion.
   */
  def searchProfileFromResource(URI:String) :SearchProfile = {
    val sp = SearchProfile(resourceAsXML(URI))
    assert(sp.isInstanceOf[Some[SearchProfile]],
           "Profile "+sp+" not valid")
    sp.asInstanceOf[Some[SearchProfile]].get
  }


}
