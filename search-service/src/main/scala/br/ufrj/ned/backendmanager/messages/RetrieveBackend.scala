package br.ufrj.ned.backendmanager.messages


/**
 * This message is used to retrieve the SearchBackend with index id.
 * 
 * Usage : val reply = BackendManager !? new RetrieveBackend(3)
 */
case class RetrieveBackend(id : Int) 