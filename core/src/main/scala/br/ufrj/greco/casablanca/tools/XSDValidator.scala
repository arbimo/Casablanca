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
 
package br.ufrj.greco.casablanca.tools

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