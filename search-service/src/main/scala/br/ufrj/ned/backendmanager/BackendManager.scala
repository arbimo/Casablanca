package br.ufrj.ned.backendmanager

import com.codahale.logula.Logging
import java.io.File
import scala.collection.JavaConversions._
import scala.actors.Actor
import scala.collection.mutable.ArrayBuffer
import br.ufrj.ned.searchbackend._
import br.ufrj.ned.backendmanager.messages._
import br.ufrj.ned.exceptions._

/**
 * This backend manager provides a thread safe way to manage backends.
 * 
 * It comes with public methods to retrieve, add or list backends that you're 
 * expected to use.
 * 
 * It is implemented by using an actor reacting to messages presents in the 
 * messages package.
 */
object BackendManager extends Actor with Logging {

  /**
   * Stores every search backend available.
   * 
   * No backend should be remove from this list to make sure a request
   * by index will always give the same backend.
   */
  private val backends = new ArrayBuffer[SearchProfile](0)

  /**
   * Index of the default backend to use.
   * 
   * Should point on last file called "default.xml" that was added.
   * Should point on the first backend otherwise.
   */
  private var defaultBackend = 0

  /**
   * This method is used to load every XML file of a directory in the 
   * availables backends
   * 
   * @param dir The directory to search
   */
  def loadFromDir(dir : String) {
    BackendManager ! LoadFromDir(dir)
  }

  /**
   * This method is used to retrieve the default profile.
   */
  def retrieveDefault : SearchProfile =
    BackendManager !? RetrieveDefault match {
      case Some(sb:SearchProfile) => sb
      case _ => throw new ProfileNotFoundException
    }

  /** 
   * This method is used to retrieve the profile by furnishing its id.
   * 
   * @param id The index of the profile. (position in internal list)
   */
  def retrieveBackend(id : Int) : SearchProfile =
    BackendManager !? RetrieveBackend(id) match {
      case Some(sb:SearchProfile) => sb
      case _ => throw new ProfileNotFoundException
    }

  /**
   * This method is used to set a backend as a default by furnishing its id
   * 
   * @param id The index of the profile. (position in internal list)
   */
  def setDefault(id:Int) {
    BackendManager !? SetDefault(id) match {
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
    BackendManager !? GetList match {
      case list : List[_] => list.map(_.asInstanceOf[SearchProfile])
      case _ => Nil
    }

  /**
   * Halt the backend manager.
   * 
   * Calling this method make it quit the act() method. Therefore it won't reply
   * to any message or public method call
   */
  def stop {
    BackendManager ! 'quit
  }

  /**
   * This method is used to load every XML file of a directory in the 
   * availables backends
   * 
   * @param dir The directory to search
   */
  private def privLoadFromDir(dir : File) {
    try {
      if(!dir.isDirectory) 
        throw new Exception("Parameter is not a directory" + dir)

      val fileList = dir.listFiles.map(_.getPath)
      for(file <- fileList ; if file.endsWith(".xml")) {
        log.info("Adding %s to backends", file)
        
        SearchProfile(file) match { 
          case Some(sb) => {
              backends.append(sb)
              if(file.endsWith("default.xml"))
                defaultBackend = backends.length - 1
          }
          case None => log.warn("Unable to load config file %s", file)
        }
      }
    } catch {
      case e => 
        log.error("Can't load backends from directory : %s", e)
    }
  }

  override def toString() : String = {
    val list = BackendManager.getList
    var listStr = ""
    for(i <- 0 to list.length-1) {
      if(i==defaultBackend)
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
        case RetrieveBackend(id) => 
          if(id>=0 && id < backends.length)
            reply(Some(backends(id)))
          else
            reply(None)
          
        case RetrieveDefault =>
          if(defaultBackend < backends.length)
            reply(Some(backends(defaultBackend)))
          else
            reply(None)

        case SetDefault(id) =>
          if(id>=0 && id < backends.length) {
            defaultBackend = id
            reply(Some(id))
          } else {
            log.warn("Request backend is not in availables one. Id : %d", id)
            reply(None)
          }

        case LoadFromDir(dir) => privLoadFromDir(new File(dir))

        case GetList => reply(backends.toList)
          
        case 'quit => exit()

        case msg => log.warn("Unrecognised message %s", msg)
      }
    }
  }
}
