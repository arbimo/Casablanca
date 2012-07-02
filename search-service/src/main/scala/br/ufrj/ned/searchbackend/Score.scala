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

  override def toString = label+":"+normalizedValue

  def toXML = 
    <score label={label}>{normalizedValue}</score>

}

object Score {

  /**
   * Given a set of scores (represented as float), this method creates a 
   * pure-functions that, given a raw score, computes the associated normalized 
   * score.
   */
  def normalizationFunction(scores:Seq[Float]) : (Float => Float) = {
    var min = Float.MaxValue
    var max = Float.MinValue
    for(s <- scores) {
      if(s < min)
        min = s
      if(s > max)
        max = s
    }

    assert(scores.isEmpty || min>=0f, "Minimum value is biger than 0")

    return s => if(max == min) 5f
                else (s-min)*8f / (max-min)  +1f
  }
}
