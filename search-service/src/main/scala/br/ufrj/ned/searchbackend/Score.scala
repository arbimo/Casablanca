package br.ufrj.ned.searchbackend

/**
 * This class consists of a single score value associated with a search result.
 * 
 * It consists of the label associated with the popularity retrievment method,
 * the measured raw value and a normalized value.
 */
class Score(val label:String, val rawValue:Float, val normalizedValue:Float) {

  def this(label:String, value:Float) =
    this(label, value, value)

  override def toString = label+":"+rawValue

  def toXML = 
    <score label={label}>{normalizedValue}</score>

}
