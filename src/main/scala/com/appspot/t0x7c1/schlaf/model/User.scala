package com.appspot.t0x7c1.schlaf.model

import com.google.appengine.api.{datastore => ds}
import com.appspot.t0x7c1.zelt.core.gae

class UserNotFoundException(val target: String) extends Exception

class User (
  val id: String,
  val nickname: String,
  val hashkey: String = UserModel.createHashkey )
{
  def update(fragment: UserFragment) =
    new User(
      id = id,
      nickname = fragment.nickname.getOrElse(nickname),
      hashkey = fragment.hashkey.getOrElse(hashkey)
    )

  private[model] def toEntity: ds.Entity = {
    val key = ds.KeyFactory.createKey(UserModel.kind, id)
    val entity = new ds.Entity(key)
    val data = Map(
      "id" -> id,
      "nickname" -> nickname,
      "hashkey" -> hashkey
    )
    data foreach { case (k, v) => entity.setProperty(k, v) }
    entity
  }

}

class UserFragment (
  var nickname: Option[String] = None,
  var hashkey: Option[String] = None )
{
  def nickname_= (str: String) { nickname = Option(str) }
  def hashkey_= (str: String) { hashkey = Option(str) }
}

object UserModel extends gae.Logger{
  import java.util.ConcurrentModificationException
  import java.util.UUID

  def kind = "user"

  def getService = ds.DatastoreServiceFactory.getDatastoreService

  def createHashkey = UUID.randomUUID.toString

  def count = {
    val query = new ds.Query(kind)
    getService.prepare(query).countEntities
  }

  def create (item: User): Boolean = {
    val service = new InTransaction(getService)
    service.notFound(item) && service.commit(item)
  }

  def update(item: User): Boolean = {
    val service = new InTransaction(getService)
    service.notModified(item) && service.commit(item)
  }

  def get (id: String): User = find(id) match {
    case Some(item) => item
    case None => throw new UserNotFoundException(id)
  }

  def find (id: String): Option[User] = {
    val query = new ds.Query(kind)
    query.addFilter("id", ds.Query.FilterOperator.EQUAL, id)

    val prepared = getService.prepare(query)
    prepared.asSingleEntity match {
      case null => None
      case entity => Some(entityToItem(entity))
    }
  }

  protected class InTransaction(service: ds.DatastoreService){
    val transaction = service.beginTransaction

    def notModified(item: User) =
      try{
        val entity = service.get(transaction, item.toEntity.getKey)
        val current = entityToItem(entity)
        if (current.hashkey != item.hashkey)
          throw new ConcurrentModificationException
        else true
      } catch {
        case e: ds.EntityNotFoundException => true
      }

    def notFound(item: User) =
      try{
        service.get(transaction, item.toEntity.getKey)
        transaction.rollback
        false
      } catch {
        case e: ds.EntityNotFoundException => true
      }

    def commit(item: User) =
      try{
        val fragment = new UserFragment(
          hashkey = Some(createHashkey)
        )
        val entity = item.update(fragment).toEntity
        service.put(transaction, entity)
        transaction.commit
        true
      } catch {
        case e: ConcurrentModificationException =>
          if (transaction.isActive) transaction.rollback
          throw e
      }

  }

  private[model] def entityToItem(entity: ds.Entity): User = {
    val -- = new {
      def as[A] = entity.getProperty(_: String).asInstanceOf[A]
      val asString = as[String]
    }
    new User(
      id = --asString "id",
      nickname = --asString "nickname",
      hashkey = --asString "hashkey"
    )
  }

}
