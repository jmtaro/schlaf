package com.appspot.t0x7c1.zelt.model

import com.google.appengine.api.{datastore => ds}
import java.util.ConcurrentModificationException
import java.util.UUID

import com.appspot.t0x7c1.zelt.model


abstract class Item

abstract class UniqueItem extends Item{
  def id: String
  def hashkey: String
}

private class TransactionService(
  service: ds.DatastoreService, transaction: ds.Transaction )
{
  def get (key: ds.Key) = { service.get(transaction, key) }
  def put (entity: ds.Entity) { service.put(transaction, entity) }
}

private object Operation {
  def getService = ds.DatastoreServiceFactory.getDatastoreService

  def count (query: ds.Query): Int = getService.prepare(query).countEntities

  def transact (block: TransactionService => Unit){
    val service = getService
    val transaction = service.beginTransaction
    try{
      block(new TransactionService(service, transaction))
      transaction.commit
    } catch {
      case e: Exception =>
        if (transaction.isActive) transaction.rollback
        throw e
    }
  }

}

trait ItemConverter {
  type Item <: model.Item

  def entityToItem(entity: ds.Entity): Item
  def itemToEntity(item: Item): ds.Entity
  def itemToKey(item: Item): ds.Key

  implicit def convertFromItem(item: Item) = new {
    def toEntity = itemToEntity(item)
    def toKey = itemToKey(item)
  }

  implicit def convertFromEntity(entity: ds.Entity) = new {
    def toItem = entityToItem(entity)
  }

}

class PropertyInspector(entity: ds.Entity){
  def as [A] = entity.getProperty(_: String).asInstanceOf[A]
  def asString = as[String]
  def asDate = as[java.util.Date]
  def set [A](kv: (String, A)) = entity.setProperty(kv._1, kv._2)
}

trait UniqueItemModel extends ItemConverter{
  type Item <: model.UniqueItem

  def rebuild(item: Item): Item

  def kind: String

  def createHashkey = UUID.randomUUID.toString

  def itemToKey(item: Item): ds.Key = ds.KeyFactory.createKey(kind, item.id)

  def count = Operation count new ds.Query(kind)

  def put (item: Item) = Operation.transact{ service =>
    try{
      val current = service.get(item.toKey).toItem
      if (current.hashkey != item.hashkey)
        throw new ConcurrentModificationException
    } catch {
      case e: ds.EntityNotFoundException =>// noop
    }
    service put rebuild(item).toEntity
  }

  def find (id: String): Option[Item] = {
    val service = Operation.getService
    try {
      val key = ds.KeyFactory.createKey(kind, id)
      Option(service.get(key).toItem)
    } catch {
      case e: ds.EntityNotFoundException => None
    }
  }

  protected def toEntity (item: Item)(block: PropertyInspector => Unit): ds.Entity = {
    val entity = new ds.Entity(item.toKey)
    val inspector = new PropertyInspector(entity)
    block(inspector)
    entity
  }

  protected def toItem [A <: Item](entity: ds.Entity)(block: PropertyInspector => A): A = {
    val inspector = new PropertyInspector(entity)
    block(inspector)
  }

}

