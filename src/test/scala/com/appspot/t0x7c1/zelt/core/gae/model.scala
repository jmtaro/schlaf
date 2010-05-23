package com.appspot.t0x7c1.zelt.core.gae.model

import com.google.appengine.tools.development.testing.{
  LocalServiceTestHelper, LocalDatastoreServiceTestConfig
}
import com.google.appengine.api.datastore.{
  DatastoreServiceFactory, Entity, Query
}
import org.scalatest.{
  FunSuite, BeforeAndAfterEach
}

class SampleTestSuite extends FunSuite with BeforeAndAfterEach{
  val config = new LocalDatastoreServiceTestConfig
  val helper = new LocalServiceTestHelper(config)

  override def beforeEach {
    helper.setUp
  }

  override def afterEach {
    helper.tearDown
  }

  test("sample test"){
    val service = DatastoreServiceFactory.getDatastoreService
    val kind = "hogehoge"
    service put new Entity(kind)
    val query = new Query(kind)
    expect(1){ service.prepare(query).countEntities }
  }

  test("sample test2"){
    val service = DatastoreServiceFactory.getDatastoreService
    val kind = "hogehoge"
    service put new Entity(kind)
    service put new Entity(kind)
    val query = new Query(kind)
    expect(2){ service.prepare(query).countEntities }
  }

}

