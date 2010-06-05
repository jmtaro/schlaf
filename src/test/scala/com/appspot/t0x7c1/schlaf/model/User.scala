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
    expect(true){ UserModel put user1 }

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

    val nickname2 = "new-nickname"
    val fragment = new UserFragment(nickname = Some(nickname2))
    val userUpdated = UserModel.get(id).update(fragment)

    UserModel put userUpdated

    val user2 = UserModel.get(id)

    // 値が更新されていればおｋ
    expect(nickname2){ user2.nickname }

    // ハッシュキーも更新されている
    assert(user.hashkey != user2.hashkey)
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

    val user = UserModel get id

    // 更新に失敗しているので値に変化なし
    assert(user.nickname == nickname)
    assert(user.nickname != nickname2)
  }

}

