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

  test("create & find"){
    val id = "sample-id"
    val user = new User(
      id = id,
      nickname = "sample-nickname"
    )
    UserModel create user

    // datastore 内に作成されている
    val data = UserModel.find(id)
    assert(data != None)

    // 更新後の値と一致する
    val entity = data.get
    assert(user.id == entity.id)
    assert(user.nickname == entity.nickname)

    // Date 型の変数あり
    assert(entity.updated != null)
  }

  test("create : fail (duplicate)"){
    val id = "sample-id"

    val user1 = new User(
      id = id,
      nickname = "sample-nickname"
    )
    expect(true){ UserModel create user1 }

    val user2 = new User(
      id = id,
      nickname = "sample-nickname2"
    )
    // 既に user1 が存在するので create 失敗
    expect(false){ UserModel create user2 }

    // 多重に create されていない
    expect(1){ UserModel.count }

    val item = UserModel.find(id).get

    // 更新後の値と一致すればおｋ
    assert(user1.id == item.id)
    assert(user1.nickname == item.nickname)
  }

  test("update : success"){
    val id = "sample-id"
    val nickname = "sample-nickname"

    UserModel create new User(
      id = id,
      nickname = nickname
    )

    val nickname2 = "new-nickname"
    val fragment = new UserFragment(nickname = Some(nickname2))
    val userUpdated = UserModel.get(id).update(fragment)

    UserModel update userUpdated

    // 値が更新されていればおｋ
    expect(nickname2){ UserModel.get(id).nickname }
  }

  test("update : ConcurrentModificationException"){
    val id = "sample-id"
    val nickname = "sample-nickname"
    UserModel create new User(
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
      UserModel update user2
    }

    val user = UserModel get id

    // 更新に失敗しているので値に変化なし
    assert(user.nickname == nickname)
    assert(user.nickname != nickname2)
  }

}

