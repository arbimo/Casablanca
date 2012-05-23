package br.ufrj.ned.backendmanager

import com.codahale.logula.Logging
import java.io.File
import scala.collection.JavaConversions._
import scala.actors.Actor
import scala.collection.mutable.ArrayBuffer
import br.ufrj.ned.searchbackend._

/**
 * This message is used to retrieve the default SearchBackend
 * 
 * Usage : val reply = BackendManager !? new RetrieveDefault
 */
case object RetrieveDefault

/**
 * This message is used to retrieve the SearchBackend with index id.
 * 
 * Usage : val reply = BackendManager !? new RetrieveBackend(3)
 */
case class RetrieveBackend(id : Int) 

/**
 * This message is used to load every XML file of a directory in the 
 * availables backends
 */
case class LoadFromDir(dir : String)

/**
 * This backend manager provides a way to manage backends in a
 * thread safe way.
 */
object BackendManager extends Actor with Logging {

  /**
   * Stores every search backend available.
   * 
   * No backend should be remove from this list to make sure a request
   * by index will always give the same backend.
   */
  private val backends = new ArrayBuffer[SearchBackend](0)

  /**
   * Index of the default backend to use.
   * 
   * Should point on last file called "default.xml" that was added.
   * Should point on the first backend otherwise.
   */
  private var defaultBackend = 0

  // TODO : cleaner start up process
  loadFromDir(new File(System.getenv("UFRJ_NED_CONF")))
  start()


  /**
   * This message is used to load every XML file of a directory in the 
   * availables backends
   * 
   * @param dir The directory to search
   */
  private def loadFromDir(dir : File) {
    try {
      if(!dir.isDirectory) 
        throw new Exception("Parameter is not a directory")

      val fileList = dir.listFiles.map(_.getPath)
      for(file <- fileList ; if file.endsWith(".xml")) {
        log.info("Adding %s to backends", file)
        
        SearchBackend(file) match { 
          case Some(sb) => {
              backends.append(sb)
              if(file.endsWith("default.xml"))
                defaultBackend = backends.length - 1
          }
          case None => log.warn("Unable to load config file %s", file)
        }
        println(backends.length)
      }
    } catch {
      case e => 
        log.error("Can't load backends from directory : %s", e)
    }
  }

  //TODO : make it thread safe
  override def toString() : String = {
    var listStr = ""
    for(i <- 0 to backends.length-1) {
      if(i==defaultBackend)
        listStr += "-> "
      else
        listStr += "   "
      listStr += i + " - " + backends(i).name +"\n"
    }
    listStr
  }

  override def act() {
    loop {
      react {
        case RetrieveBackend(id) => 
          if(id>=0 && id < backends.length)
            reply(backends(id))
          else
            reply(None)
          
        case RetrieveDefault =>
          if(defaultBackend < backends.length)
            reply(backends(defaultBackend))
          else
            reply(None)

        case LoadFromDir(dir) => loadFromDir(new File(dir))
          
        case 'quit => exit()

        case msg => log.warn("Unrecognised message %s", msg)
      }
    }
  }
}
