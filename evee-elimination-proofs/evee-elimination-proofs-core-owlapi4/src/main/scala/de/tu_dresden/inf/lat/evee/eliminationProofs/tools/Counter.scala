package de.tu_dresden.inf.lat.evee.eliminationProofs.tools

import scala.collection.mutable


class Counter[T] {

  private val innerMap = new mutable.HashMap[T,Integer]()

  def add(thing: T): Unit ={
    if(!innerMap.contains(thing))
      innerMap.put(thing, 1)
    else
      innerMap.put(thing, innerMap(thing)+1)
  }

  def get(thing: T) =
    innerMap.getOrElse(thing, 0)

  def keys = innerMap.keys

  def removeKey(key: T) =
    innerMap.remove(key)

  def removeKeys(keys: Iterable[T]) =
    keys.foreach(removeKey)


  def empty = innerMap.isEmpty

  def max =
    innerMap.maxBy(_._2)._1

  def toMap = innerMap.toMap[T,Integer]

  def filter(function: (T => Boolean)) = {
    val result = new Counter[T]()
    result.innerMap++= innerMap.filterKeys(function)
    result
  }
}
