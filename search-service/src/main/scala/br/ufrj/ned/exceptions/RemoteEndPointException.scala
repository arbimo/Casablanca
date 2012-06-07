package br.ufrj.ned.exceptions

/**
 * This exception is thrown when an error occured while contacting
 * or querying the remote SPARQL end point.
 */
class RemoteEndPointException(message:String) 
      extends Exception("Remote end point error : " + message)

