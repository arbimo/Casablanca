package br.ufrj.ned.restapi

import javax.ws.rs._
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.profilemanager._
import br.ufrj.ned.exceptions._
import net.liftweb.json.JsonAST._

@Path("/search/{searchTerm}")
class SearchService extends WebService {

  override val jsonTransform : PartialFunction[JValue, JValue] = { 
    case JField("global-score", JString(s)) => JField("global-score", JDouble(s.toDouble))
    case JField("score", x:JObject) => JField("score", JArray(x :: Nil))
    case JField("search-result", x: JObject) => JField("search-result", JArray(x :: Nil))
    case JField("property", x: JObject) => JField("property", JArray(x :: Nil))
  }

  /**
   * Perform a search for the given search term on the given profile id.
   * If profileId is -1, the default profile is used.
   * 
   * Result is given as an XML serialization of SearchResults objects.
   */
  def performSearch(searchTerm:String, profileId:Int) : scala.xml.NodeSeq = {
    try {
      val sb = {
        if(profileId == -1)
          ProfileManager.retrieveDefault
        else
          ProfileManager.retrieveProfile(profileId)
      }
      val resultSeq = sb.search(searchTerm)
      
      <search term={searchTerm}>{resultSeq.map(res => res.toXML)}</search>
      
    } catch {
      case e:ProfileNotFoundException => 
        throw new ProfileNotFoundWebException(profileId)
      case e:RemoteEndPointException =>
        throw new RemoteEndPointWebException(e.toString)
    }
  }

  @GET @Produces(Array("application/xml"))
  def doGetXML(@PathParam("searchTerm") searchTerm:String,
            @DefaultValue("-1") @QueryParam("profile") profileId:Int) = {

    ok(performSearch(searchTerm, profileId).toString)
  }

  @GET @Produces(Array("application/json"))
  def doGetJSON(@PathParam("searchTerm") searchTerm:String,
            @DefaultValue("-1") @QueryParam("profile") profileId:Int) = {

    ok(json(performSearch(searchTerm, profileId)))
  }
  
  
}