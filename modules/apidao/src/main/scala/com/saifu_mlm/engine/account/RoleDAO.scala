package com.saifu_mlm.engine.account

import scala.concurrent.Future

trait RoleDAO {

  def lookUp(id: String): Future[Option[Role]]

  def all: Future[Seq[Role]]

  def update(role: Role): Future[Int]

  def delete(id: String): Future[Int]

  def create(role: Role): Future[Role]

  def close(): Future[Unit]

}

case class Role(
    id: String = "",
    name: String,
    explain: Option[String]
)
