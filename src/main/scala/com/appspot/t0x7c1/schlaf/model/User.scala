package com.appspot.t0x7c1.schlaf.model

import com.google.appengine.api.{datastore => ds}
import com.appspot.t0x7c1.zelt.core.gae
import com.appspot.t0x7c1.zelt.{model => md}

class UserNotFoundException(val target: String) extends Exception

class User (
  val id: String,
  val nickname: String,
  val hashkey: String = UserModel.createHashkey ) extends md.UniqueItem
{
  def update(fragment: UserFragment) = UserModel.update(this, fragment)
}

class UserFragment (
  var nickname: Option[String] = None ) extends md.UniqueItemFragment

object UserModel extends md.UniqueItemModel with gae.Logger{
  override type Item = User
  override type Fragment = UserFragment

  def kind = "user"

  def createFragment = new UserFragment

  def update(item: Item, fragment: Fragment): Item =
    new User(
      id = item.id,
      nickname = fragment.nickname getOrElse item.nickname,
      hashkey = fragment.hashkey getOrElse item.hashkey
    )

  def entityToItem(entity: ds.Entity): Item = {
    val -- = new PropertyInspector(entity)
    new User(
      id = --asString "id",
      nickname = --asString "nickname",
      hashkey = --asString "hashkey"
    )
  }

  def itemToEntity(item: Item): ds.Entity = {
    val key = ds.KeyFactory.createKey(UserModel.kind, item.id)
    val entity = new ds.Entity(key)
    val prop = new PropertyInspector(entity)
    prop set "id" -> item.id
    prop set "nickname" -> item.nickname
    prop set "hashkey" -> item.hashkey
    entity
  }

}
