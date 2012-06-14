package br.ufrj.ned.tools

import com.codahale.logula.Logging
import java.io.InputStream
import java.io.StringReader
import javax.xml.transform.sax.SAXSource
import javax.xml.transform.stream.StreamSource
import javax.xml.validation.SchemaFactory
import org.xml.sax.InputSource
import org.xml.sax.SAXException

object XSDValidator extends Logging {

  def validate(xmlString: String, xsd: InputStream): Boolean = {
    try {
      val schemaLang = "http://www.w3.org/2001/XMLSchema"
      val factory = SchemaFactory.newInstance(schemaLang)
      val schema = factory.newSchema(new StreamSource(xsd))
      val validator = schema.newValidator()
      validator.validate(new SAXSource(new InputSource(new StringReader(xmlString))))
    } catch {
      case ex: SAXException =>
        log.warn("XML is not valid : %s", ex.getMessage())
        return false
      case ex: Exception => 
        log.warn("XML is not conform to schema : %s",ex.getMessage)
        return false
    }
    true
  }
}