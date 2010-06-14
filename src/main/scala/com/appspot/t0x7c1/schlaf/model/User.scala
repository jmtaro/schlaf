package com.appspot.t0x7c1.schlaf.model

import com.google.appengine.api.{datastore => ds}
import com.appspot.t0x7c1.zelt.core.gae
import com.appspot.t0x7c1.zelt.{model => md}


case class User (
  val id: String,
  val nickname: String,
  val hashkey: String = UserModel.createHashkey ) extends md.UniqueItem

object UserData {
  def find = UserModel.find _
  def put = UserModel.put _
  def count = UserModel.count
}

private[model] object UserModel extends md.UniqueItemModel with gae.Logger{
  type Item = User

  def kind = "user"

  def rebuild(item: Item) = item.copy(hashkey = createHashkey)

  def itemToEntity(item: Item) = toEntity(item){ -- =>
    --set "id" -> item.id
    --set "nickname" -> item.nickname
    --set "hashkey" -> item.hashkey
  }

  def entityToItem(entity: ds.Entity) = toItem(entity){ -- =>
    new User(
      id = --asString "id",
      nickname = --asString "nickname",
      hashkey = --asString "hashkey" )
  }

}

