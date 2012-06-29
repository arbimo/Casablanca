package br.ufrj.ned.searchbackend

/**
 * This class describes a method to retrieve a popularity measure.
 * 
 * This method consists in looking for the value associate with a predicate for each candidate
 */
case class PopularityMethod(val predicate:URI, val label:String) {

  def this(pred:String, label:String) = this(new URI(pred), label)
  
  override def toString : String = label+" : "+predicate

  def toXML = 
    <measure>
      <label>{label}</label>
      <predicate>{predicate}</predicate>
    </measure>
}

object PopularityMethod {

  /**
   * Takes <measure> node and returns the corresponding PopularityMethod
   * instance.
   */
  def apply(measureNode : scala.xml.Node) : PopularityMethod = {
    val pred = (measureNode\"predicate").text
    val label =
      if((measureNode\"label").isEmpty)
        "popularity"
      else
        (measureNode\"label").text

    new PopularityMethod(new URI(pred), label)
  }
}
