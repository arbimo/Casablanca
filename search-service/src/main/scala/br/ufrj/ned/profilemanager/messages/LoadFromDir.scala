package br.ufrj.ned.profilemanager.messages

/**
 * This message is used to load every XML file of a directory in the 
 * availables profiles 
 */
case class LoadFromDir(dir : String)