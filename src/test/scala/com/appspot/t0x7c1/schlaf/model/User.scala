package com.appspot.t0x7c1.schlaf.model

import com.google.appengine.tools.development.testing.{
  LocalServiceTestHelper, LocalDatastoreServiceTestConfig
}

import org.scalatest.{
  FunSuite, BeforeAndAfterEach
}

class UserModelTestSuite extends FunSuite with BeforeAndAfterEach{
  val config = new LocalDatastoreServiceTestConfig
  val helper = new LocalServiceTestHelper(config)

  override def beforeEach {
    helper.setUp
  }

  override def afterEach {
    helper.tearDown
  }

  test("put & find"){
    val id = "sample-id"
    val user = new User(
      id = id,
      nickname = "sample-nickname"
    )
    UserModel put user

    // datastore 内に格納されている
    val data = UserModel.find(id)
    assert(data != None)

    // 作成した値と一致する
    val entity = data.get
    assert(user.id == entity.id)
    assert(user.nickname == entity.nickname)

    // String 型の変数あり
    assert(entity.hashkey != null)
    assert(entity.hashkey.isInstanceOf[String])
  }

  test("put : duplicate-error"){
    val id = "sample-id"

    val user1 = new User(
      id = id,
      nickname = "sample-nickname"
    )
    UserModel put user1

    val user2 = new User(
      id = id,
      nickname = "sample-nickname2"
    )

    // 既に user1 が存在するので put 失敗
    intercept[java.util.ConcurrentModificationException]{
      UserModel put user2
    }

    // 多重に格納されていない
    expect(1){ UserModel.count }

    val item = UserModel.find(id).get

    // 格納した値と一致すればおｋ
    assert(user1.id == item.id)
    assert(user1.nickname == item.nickname)
  }

  test("put : success"){
    val id = "sample-id"
    val nickname = "sample-nickname"

    val user = new User(
      id = id,
      nickname = nickname
    )
    UserModel put user
    expect(1){ UserModel.count }

    val nickname2 = "new-nickname"

    // update
    UserModel.find(id) match {
      case None => assert(false)
      case Some(u) =>
        val fr = new UserFragment(nickname = Some(nickname2))
        UserModel put u.update(fr)
    }

    // confirm
    UserModel.find(id) match {
      case None => assert(false)
      case Some(u) =>
        expect(nickname2){ u.nickname }// 値が更新されていればおｋ
        assert(user.hashkey != u.hashkey)// ハッシュキーも更新されている
    }
    expect(1){ UserModel.count }

    UserModel put new User(
      id = "sample-id-2",
      nickname = "new nickname foobar"
    )
    expect(2){ UserModel.count }

  }

  test("put : ConcurrentModificationException"){
    val id = "sample-id"
    val nickname = "sample-nickname"
    UserModel put new User(
      id = id,
      nickname = nickname
    )

    val nickname2 = "new-nickname"
    val user2 = new User(
      id = id,
      nickname = nickname2
    )

    // user2 は datastore 内の entity と異なるため必ず失敗
    intercept[java.util.ConcurrentModificationException]{
      UserModel put user2
    }

    UserModel.find(id) match {
      case None => assert(false)
      case Some(u) =>
        // 更新に失敗しているので値に変化なし
        assert(u.nickname == nickname)
        assert(u.nickname != nickname2)
    }


  }

}

