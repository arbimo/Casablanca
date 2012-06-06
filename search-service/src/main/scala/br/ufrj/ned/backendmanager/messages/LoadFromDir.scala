package br.ufrj.ned.backendmanager.messages

/**
 * This message is used to load every XML file of a directory in the 
 * availables backends
 */
case class LoadFromDir(dir : String)