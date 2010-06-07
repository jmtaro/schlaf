package com.appspot.t0x7c1.zelt.model

import com.google.appengine.api.{datastore => ds}
import java.util.ConcurrentModificationException
import java.util.UUID

abstract class BasicItem

abstract class BasicItemFragment

abstract class BasicItemModel{
  type Item <: BasicItem
  type Fragment <: BasicItemFragment

  def kind: String
  def createFragment: Fragment
  def update(item: Item, fragment: Fragment): Item

  def itemToEntity(item: Item): ds.Entity
  def entityToItem(entity: ds.Entity): Item

  def count: Int = {
    val query = new ds.Query(kind)
    getService.prepare(query).countEntities
  }

  protected def getService = ds.DatastoreServiceFactory.getDatastoreService

  protected class PropertyInspector(entity: ds.Entity){
    def as[A] = entity.getProperty(_: String).asInstanceOf[A]
    def asString = as[String]
    def asDate = as[java.util.Date]
    def set [A](kv: (String, A)) = entity.setProperty(kv._1, kv._2)
  }

  protected class StartTransaction{
    lazy val service = ds.DatastoreServiceFactory.getDatastoreService
    lazy val transaction = service.beginTransaction
  }

  protected trait InTransaction {
    val service: ds.DatastoreService
    val transaction: ds.Transaction
  }

  protected trait BasicItemSetter extends InTransaction{
    def put (item: Item) {
      try{
        val entity = itemToEntity(item)
        service.put(transaction, entity)
        transaction.commit
      } catch {
        case e: ConcurrentModificationException =>
          if (transaction.isActive) transaction.rollback
          throw e
      }
    }
  }

  protected trait BasicItemGetter extends InTransaction{
    def notFound (item: Item): Boolean =
      try{
        service.get(transaction, itemToEntity(item).getKey)
        transaction.rollback
        false
      } catch {
        case e: ds.EntityNotFoundException => true
      }
  }

}

abstract class UniqueItem extends BasicItem{
  def hashkey: String
}

abstract class UniqueItemFragment extends BasicItemFragment{
  var hashkey: Option[String] = None
}

abstract class UniqueItemModel extends BasicItemModel{
  type Item <: UniqueItem
  type Fragment <: UniqueItemFragment

  def put(item: Item) {
    val op = new StartTransaction with UniqueItemSetter with UniqueItemGetter
    op confirmNotModified item
    op put item
  }

  def createHashkey = UUID.randomUUID.toString

  def find (id: String): Option[Item] = {
    val query = new ds.Query(kind)
    query.addFilter("id", ds.Query.FilterOperator.EQUAL, id)

    getService.prepare(query).asSingleEntity match {
      case null => None
      case entity => Some(entityToItem(entity))
    }
  }

  protected trait UniqueItemGetter extends InTransaction{
    def confirmNotModified (item: Item) {
      try{
        val entity = service.get(transaction, itemToEntity(item).getKey)
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

  protected trait UniqueItemSetter extends BasicItemSetter{
    override def put (item: Item) {
      val fragment = createFragment
      fragment.hashkey = Some(createHashkey)
      super.put(update(item, fragment))
    }
  }

}

