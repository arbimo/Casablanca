package br.ufrj.ned.profilemanager

import br.ufrj.ned.searchbackend.SearchProfile
import org.xml.sax.InputSource

object ProfileUtils {

  val profiles = List("/profiles/dbpedia.xml", 
                      "/profiles/dbpedia-cities.xml",
                      "/profiles/default.xml")

  def resourceAsXML(URI:String) = {
    val configIn = this.getClass.getResourceAsStream("/profiles/dbpedia.xml")
    assert(configIn != null, "Profile resource not found")
    scala.xml.XML.load(new InputSource(configIn))
  }

  def searchProfileFromResource(URI:String) :SearchProfile = {
    val sp = SearchProfile(resourceAsXML(URI))
    assert(sp.isInstanceOf[Some[SearchProfile]],
           "Profile "+sp+" not valid")
    sp.asInstanceOf[Some[SearchProfile]].get
  }


}
