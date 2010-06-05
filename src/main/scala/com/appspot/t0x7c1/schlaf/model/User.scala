package com.appspot.t0x7c1.schlaf.model

import com.google.appengine.api.{datastore => ds}
import com.appspot.t0x7c1.zelt.core.gae
import java.util.UUID

class UserNotFoundException(val target: String) extends Exception

class User (
  val id: String,
  val nickname: String,
  val hashkey: String = UserModel.createHashkey )
{
  def update(fragment: UserFragment): User =
    new User(
      id = id,
      nickname = fragment.nickname.getOrElse(nickname),
      hashkey = fragment.hashkey.getOrElse(hashkey)
    )

  def update(uuid: UUID): User =
    this update new UserFragment(hashkey = Some(uuid.toString))

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

  def kind = "user"

  protected def getService = ds.DatastoreServiceFactory.getDatastoreService

  def createHashkey = UUID.randomUUID.toString

  def count = {
    val query = new ds.Query(kind)
    getService.prepare(query).countEntities
  }

  def put(item: User) {
    val op = new InTransaction with UniqueItemSetter with UniqueItemGetter
    op confirmNotModified item
    op put item
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

  protected class InTransaction{
    lazy val service = ds.DatastoreServiceFactory.getDatastoreService
    lazy val transaction = service.beginTransaction
  }

  protected abstract trait TransactionOp {
    val service: ds.DatastoreService
    val transaction: ds.Transaction
  }

  protected trait ItemSetter extends TransactionOp{
    def put(item: User) {
      try{
        val entity = item.toEntity
        service.put(transaction, entity)
        transaction.commit
      } catch {
        case e: ConcurrentModificationException =>
          if (transaction.isActive) transaction.rollback
          throw e
      }
    }
  }

  protected trait BasicItemGetter extends TransactionOp{
    def notFound(item: User): Boolean =
      try{
        service.get(transaction, item.toEntity.getKey)
        transaction.rollback
        false
      } catch {
        case e: ds.EntityNotFoundException => true
      }
  }

  protected trait UniqueItemSetter extends ItemSetter{
    override def put(item: User) = super.put(item update UUID.randomUUID)
  }

  protected trait UniqueItemGetter extends TransactionOp{
    def confirmNotModified(item: User) {
      try{
        val entity = service.get(transaction, item.toEntity.getKey)
        val current = entityToItem(entity)
        if (current.hashkey != item.hashkey){
          transaction.rollback
          throw new ConcurrentModificationException
        }
      } catch {
        case e: ds.EntityNotFoundException =>// noop
      }
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
