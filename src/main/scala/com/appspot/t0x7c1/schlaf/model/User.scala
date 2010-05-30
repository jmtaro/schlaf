package com.appspot.t0x7c1.schlaf.model

import com.google.appengine.api.{datastore => ds}
import com.appspot.t0x7c1.zelt.core.gae
import java.util.Date

class UserNotFoundException(val target: String) extends Exception

class User (
  val id: String,
  val nickname: String,
  val updated: Date = new Date )
{
  def update(fragment: UserFragment) =
    new User(
      id = id,
      nickname = fragment.nickname.getOrElse(nickname),
      updated = fragment.updated.getOrElse(updated)
    )

  private[model] def toEntity: ds.Entity = {
    val key = ds.KeyFactory.createKey(UserModel.kind, id)
    val entity = new ds.Entity(key)
    val data = Map(
      "id" -> id,
      "nickname" -> nickname,
      "updated" -> updated
    )
    data foreach { case (k, v) => entity.setProperty(k, v) }
    entity
  }

}

class UserFragment (
  var nickname: Option[String] = None,
  var updated: Option[Date] = None )
{
  def nickname_= (str: String) { nickname = Option(str) }
  def updated_= (date: Date) { updated = Option(date) }
}

object UserModel extends gae.Logger{
  import java.util.ConcurrentModificationException

  def kind = "user"

  def getService = ds.DatastoreServiceFactory.getDatastoreService

  def count = {
    val query = new ds.Query(kind)
    getService.prepare(query).countEntities
  }

  def create (item: User): Boolean = {
    val service = getService

    val fragment = new UserFragment
    fragment.updated = new Date

    val entity = item.update(fragment).toEntity
    val tx = service.beginTransaction

    def notFound =
      try{
        service.get(tx, entity.getKey)
        tx.rollback
        false
      } catch {
        case e: ds.EntityNotFoundException => true
      }
    def commit =
      try{
        service.put(tx, entity)
        tx.commit
        true
      } catch {
        case e: ConcurrentModificationException =>
          if (tx.isActive) tx.rollback
          throw e
      }

    notFound && commit
  }

  def create (user: User, retry: Int): Boolean =
    try {
      create(user)
    } catch {
      case e: ConcurrentModificationException =>
        if (retry > 1) create(user, retry - 1)
        else throw e
    }

  def update(item: User): Boolean = {
    val service = getService
    val tx = service.beginTransaction

    def notModified =
      try{
        val entity = service.get(tx, item.toEntity.getKey)
        val currentItem = entityToItem(entity)
        if (currentItem.updated != item.updated)
          throw new ConcurrentModificationException
        else true
      } catch {
        case e: ds.EntityNotFoundException => true
      }
    def commit =
      try{
        service.put(tx, item.toEntity)
        tx.commit
        true
      } catch {
        case e: ConcurrentModificationException =>
          tx.rollback
          throw e
      }

    notModified && commit
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

  private[model] def entityToItem(entity: ds.Entity): User = {
    val -- = new {
      def as[A] = entity.getProperty(_: String).asInstanceOf[A]
      val asString = as[String]
      val asDate = as[Date]
    }
    new User(
      id = --asString "id",
      nickname = --asString "nickname",
      updated = --asDate "updated"
    )
  }

}
