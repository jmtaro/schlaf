package com.appspot.t0x7c1.zelt.core.gae.user

import com.google.appengine.api.users

import com.google.appengine.tools.development.testing.{
  LocalServiceTestHelper, LocalUserServiceTestConfig
}

import org.scalatest.{
  FunSuite, BeforeAndAfterEach
}

class SampleTestSuite extends FunSuite with BeforeAndAfterEach{
  val config = new LocalUserServiceTestConfig
  val helper =
    new LocalServiceTestHelper(config).
      setEnvIsAdmin(true).
      setEnvIsLoggedIn(true).
      setEnvEmail("foobar@example.com").
      setEnvAuthDomain("example.com")

  override def beforeEach {
    helper.setUp
  }

  override def afterEach {
    helper.tearDown
  }

  test("sample UserService test"){
    val service = users.UserServiceFactory.getUserService
    expect(true){ service.isUserAdmin }
    expect(true){ service.isUserLoggedIn }
  }

  test("sample User test"){
    val service = users.UserServiceFactory.getUserService
    val user = service.getCurrentUser
    expect("foobar"){ user.getNickname }
    expect("foobar@example.com"){ user.getEmail }
  }

}

