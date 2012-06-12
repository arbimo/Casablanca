package br.ufrj.ned.profilemanager.messages


/**
 * This message is used to retrieve the Searchprofile with index id.
 * 
 * Usage : val reply = ProfileManager !? new RetrieveProfile(3)
 */
case class RetrieveProfile(id : Int) 