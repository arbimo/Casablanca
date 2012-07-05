package br.ufrj.ned.restapi

import javax.ws.rs.core.Response
import net.liftweb.json._

class WebService {

  /**
   * Format the the response string to enable cross domain access.
   */
  def ok(response : String ) : Response = {
    val tmp = Response.ok().entity(response).header("Access-Control-Allow-Origin","*").build();
    println(tmp)
    tmp
  }

  def json(xml : scala.xml.NodeSeq) : String = {
    val jval = Xml.toJson(xml)
    val valid = jval.transform(jsonTransform)
    Printer.pretty(render(valid))
  }

  val jsonTransform : PartialFunction[JValue, JValue] = { case x => x}

}
