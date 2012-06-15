package br.ufrj.ned.profilemanager

import br.ufrj.ned.tools.TestProfileUtils._
import br.ufrj.ned.exceptions._
import org.scalatest.junit.AssertionsForJUnit
import org.junit.Assert._
import org.junit.Test
import org.junit.After

class ProfileManagerTest extends AssertionsForJUnit {

  @After
  def stop {
    ProfileManager.clearAll
  }

  @Test
  def loadProfiles {
    for(i <- 0 to profiles.length-1) {
      val index = ProfileManager.addProfile(resourceAsXML(profiles(i)))
      assert(index===i, "At least one profile couldn't be loaded")
    }
  }

  @Test
  def retrieveProfileTest {
    intercept[ProfileNotFoundException] {
      ProfileManager.retrieveProfile(0)
    }
  }
  
}