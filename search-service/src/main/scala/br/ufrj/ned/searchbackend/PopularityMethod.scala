package br.ufrj.ned.searchbackend

/**
 * This class describes a method to retrieve a popularity measure.
 * 
 * This method consists in looking for the value associate with a predicate for each candidate
 */
case class PopularityMethod(pred:String, val label:String) extends Predicate(pred){

  override def toString : String = label+" : "+xmlUri

  def toXML = 
    <measure>
      <label>{label}</label>
      <predicate>{xmlUri}</predicate>
    </measure>

  def toSparql(subject:String) = 
    subject+" "+sparqlUri+" ?"+key

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

    new PopularityMethod(pred, label)
  }
}
