package br.ufrj.ned.profilemanager

import com.codahale.logula.Logging
import java.io.File
import scala.collection.JavaConversions._
import scala.actors.Actor
import scala.collection.mutable.ArrayBuffer
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.profilemanager.messages._
import br.ufrj.ned.exceptions._

/**
 * This profile manager provides a thread safe way to manage profiles.
 * 
 * It comes with public methods to retrieve, add or list profiles that you're 
 * expected to use.
 * 
 * It is implemented by using an actor reacting to messages presents in the 
 * messages package.
 */
object ProfileManager extends Actor with Logging {

  /**
   * Stores every search profile available.
   * 
   * No profile should be remove from this list to make sure a request
   * by index will always give the same profile.
   */
  private val profiles = new ArrayBuffer[SearchProfile](0)

  /**
   * Index of the default profile to use.
   * 
   * Should point on last file called "default.xml" that was added.
   * Should point on the first profile otherwise.
   */
  private var defaultProfile = 0

  /**
   * This method is used to load every XML file of a directory in the 
   * availables profiles
   * 
   * @param dir The directory to search
   */
  def loadFromDir(dir : String) {
    ProfileManager ! LoadFromDir(dir)
  }

  /**
   * This method is used to retrieve the default profile.
   */
  def retrieveDefault : SearchProfile =
    ProfileManager !? RetrieveDefault match {
      case Some(sb:SearchProfile) => sb
      case _ => throw new ProfileNotFoundException
    }

  /** 
   * This method is used to retrieve the profile by furnishing its id.
   * 
   * @param id The index of the profile. (position in internal list)
   */
  def retrieveProfile(id : Int) : SearchProfile =
    ProfileManager !? RetrieveProfile(id) match {
      case Some(sb:SearchProfile) => sb
      case _ => throw new ProfileNotFoundException
    }

  /**
   * This method is used to set a profile as a default by furnishing its id
   * 
   * @param id The index of the profile. (position in internal list)
   */
  def setDefault(id:Int) {
    ProfileManager !? SetDefault(id) match {
      case Some(id:Int) => 
      case _ => throw new ProfileNotFoundException
    }
  }

  /**
   * This method returns an immutable list of the available profiles.
   * 
   * The position of the profiles in the list match with their id.
   */
  def getList : List[SearchProfile] =
    ProfileManager !? GetList match {
      case list : List[_] => list.map(_.asInstanceOf[SearchProfile])
      case _ => Nil
    }

  /**
   * Halt the profile manager.
   * 
   * Calling this method make it quit the act() method. Therefore it won't reply
   * to any message or public method call
   */
  def stop {
    ProfileManager ! 'quit
  }

  /**
   * This method is used to load every XML file of a directory in the 
   * availables profiles
   * 
   * @param dir The directory to search
   */
  private def privLoadFromDir(dir : File) {
    try {
      if(!dir.isDirectory) 
        throw new Exception("Parameter is not a directory" + dir)

      val fileList = dir.listFiles.map(_.getPath)
      for(file <- fileList ; if file.endsWith(".xml")) {
        log.info("Adding %s to profiles", file)
        
        SearchProfile(file) match { 
          case Some(sb) => {
              profiles.append(sb)
              if(file.endsWith("default.xml"))
                defaultProfile = profiles.length - 1
          }
          case None => log.warn("Unable to load config file %s", file)
        }
      }
    } catch {
      case e => 
        log.error("Can't load profiles from directory : %s", e)
    }
  }

  override def toString() : String = {
    val list = ProfileManager.getList
    var listStr = ""
    for(i <- 0 to list.length-1) {
      if(i==defaultProfile)
        listStr += "-> "
      else
        listStr += "   "
      listStr += i + " - " + list(i).name +"\n"
    }
    listStr
  }

  override def act() {
    loop {
      react {
        case RetrieveProfile(id) => 
          if(id>=0 && id < profiles.length)
            reply(Some(profiles(id)))
          else
            reply(None)
          
        case RetrieveDefault =>
          if(defaultProfile < profiles.length)
            reply(Some(profiles(defaultProfile)))
          else
            reply(None)

        case SetDefault(id) =>
          if(id>=0 && id < profiles.length) {
            defaultProfile = id
            reply(Some(id))
          } else {
            log.warn("Request profile is not in availables one. Id : %d", id)
            reply(None)
          }

        case LoadFromDir(dir) => privLoadFromDir(new File(dir))

        case GetList => reply(profiles.toList)
          
        case 'quit => exit()

        case msg => log.warn("Unrecognised message %s", msg)
      }
    }
  }
}
