package com.saifu_mlm.engine.account

import java.util.UUID

import org.joda.time.DateTime

import scala.concurrent.Future

trait RoleDAO {

  def lookUp(id: String): Future[Option[Role]]

  def all: Future[Seq[Role]]

  def update(role: Role): Future[Int]

  def delete(id: String): Future[Int]

  def create(role: Role): Future[Int]

  def close(): Future[Unit]

}

case class Role(
    id: UUID,
    name: String,
    explain: Option[String],
    delete_flag: Boolean,
    created: DateTime,
    updated: Option[DateTime]
)
