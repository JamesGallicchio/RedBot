package redbot.utils

import better.files._
import File._
import play.api.libs.json._
import play.api.libs.json.Json._

import scala.collection.concurrent.TrieMap
import scala.collection.mutable

object DataStore {
  val dataRoot = currentWorkingDirectory/"data"

  def store[T](name: String, data: T)(implicit writer: Writes[T]): Unit = {
    val file = dataRoot/s"$name.txt"

    file.createIfNotExists()
      .clear()
      .write(Json.prettyPrint(Json.toJson(data)))
  }
  def get[T](name: String)(implicit reader: Reads[T]): Option[T] = {
    val file = dataRoot/s"$name.txt"

    if (file.exists)
      file.inputStream
        .map(Json.parse)
        .map(Json.fromJson[T](_))
        .get().asOpt
    else None
  }

  object Formats {
    implicit def intMapReads[V](implicit valReads: Reads[V]): Reads[Map[Int, V]] = (json: JsValue) => JsSuccess(json.as[Map[String, V]].map {
      case (k, v) => Integer.parseInt(k) -> v
    })

    implicit def intMapWrites[V](implicit valWrites: Writes[V]): Writes[Map[Int, V]] = (o: Map[Int, V]) => Json.obj(o.map {
      case (k, v) => k.toString -> (v: JsValueWrapper)
    }.toSeq: _*)

    implicit def uLongMapReads[V](implicit valReads: Reads[V]): Reads[Map[Long, V]] = (json: JsValue) => JsSuccess(json.as[Map[String, V]].map {
      case (k, v) => java.lang.Long.parseUnsignedLong(k) -> v
    })

    implicit def uLongMapWrites[V](implicit valWrites: Writes[V]): Writes[Map[Long, V]] = (o: Map[Long, V]) => Json.obj(o.map {
      case (k, v) => k.toString -> (v: JsValueWrapper)
    }.toSeq: _*)

    implicit def mutableMapReads[K, V](implicit mapReads: Reads[Map[K, V]]): Reads[TrieMap[K, V]] = mapReads.map(m => TrieMap(m.toSeq: _*))

    implicit def mutableMapWrites[K, V](implicit mapWrites: Writes[Map[K, V]]): Writes[mutable.Map[K, V]] =
      (o: mutable.Map[K, V]) => mapWrites.writes(o.toMap)
  }
}