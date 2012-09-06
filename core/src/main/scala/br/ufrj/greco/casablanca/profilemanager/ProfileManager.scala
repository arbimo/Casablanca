/**
 * Copyright 2012 Arthur Bit
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package br.ufrj.greco.casablanca.profilemanager

import com.codahale.logula.Logging
import java.io.File
import scala.collection.JavaConversions._
import scala.actors.Actor
import scala.collection.mutable.ArrayBuffer
import br.ufrj.greco.casablanca.searchbackend._
import br.ufrj.greco.casablanca.profilemanager.messages._
import br.ufrj.greco.casablanca.exceptions._

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

  start()

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
   * Load every XML file of a directory in the 
   * profile manager
   * 
   * @param dir Directory containing XML files
   */
  def loadFromDir(dir : String) {
    ProfileManager ! LoadFromDir(dir)
  }

  /**
   * Create a new profile corresponding to given XML configuration.
   * 
   * @return the ID of the newly created Profile.
   */
  def addProfile(config : scala.xml.Node) : Int = {
    ProfileManager !? AddProfileFromXML(config) match {
      case Some(i:Int) if i>= 0 => i
      case _ => throw new InvalidProfileException("Unable to load profile")
    }
  }

  /**
   * Retrieves the default profile.
   */
  def retrieveDefault : SearchProfile =
    ProfileManager !? RetrieveDefault match {
      case Some(sb:SearchProfile) => sb
      case _ => throw new ProfileNotFoundException
    }

  /** 
   * Retrieves the profile by furnishing its id.
   * 
   * @param id The index of the profile. (position in internal list)
   */
  def retrieveProfile(id : Int) : SearchProfile =
    ProfileManager !? RetrieveProfile(id) match {
      case Some(sb:SearchProfile) => sb
      case _ => throw new ProfileNotFoundException
    }

  /**
   * Set a profile as a default by furnishing its id
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
   * Returns an immutable list of the available profiles.
   * 
   * The position of the profiles in the list match with their id.
   */
  def getList : List[SearchProfile] =
    ProfileManager !? GetList match {
      case list : List[_] => list.map(_.asInstanceOf[SearchProfile])
      case _ => Nil
    }

  /**
   * Remove all existing profiles.
   */
  def clearAll {
    ProfileManager ! ClearAll
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
   * Load every XML file of a directory in the 
   * profile manager
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

  /**
   * Main execution loop
   */
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

        case AddProfileFromXML(config) =>
          SearchProfile(config) match {
            case Some(profile) => 
              profiles.add(profile)
              reply(Some(profiles.length-1))
            case None => reply(None)
          }

        case GetList => reply(profiles.toList)

        case ClearAll => profiles.clear
          
        case 'quit => exit()

        case msg => log.warn("Unrecognised message %s", msg)
      }
    }
  }
}
