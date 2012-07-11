package br.ufrj.ned.searchbackend

class PropertyPredicate(val label:String, predicate:String) 
         extends Predicate(predicate) {
  
  override val key = defaultKey + "Prop"

  def toSparql(subject:String) = subject+" "+sparqlUri+" ?"+key

  def toXML = 
    <property>
      <label>{label}</label>
      <predicate>{xmlUri}</predicate>
    </property>
}


object PropertyPredicate {

  def apply(node:scala.xml.Node) : PropertyPredicate = {
    val label = (node\"label").text
    val pred = (node\"predicate").text
    new PropertyPredicate(label, pred)
  }
}
