package com.saifu_mlm.engine.account

import java.util.UUID

import org.joda.time.DateTime

import scala.concurrent.Future

/**
  * TenantDAO : Implemented by Slick DAO
  */
trait TenantDAO {

  def lookUp(id: String): Future[Option[Tenant]]

  def all: Future[Seq[Tenant]]

  def update(tenant: Tenant): Future[Int]

  def delete(id: String): Future[Int]

  def create(tenant: Tenant): Future[Int]

  def close(): Future[Unit]

}

/**
  * Tenant Entity Case Class
  * @param id tenant id
  * @param name tenant name
  * @param explain tenant explain
  * @param delete_flag logical delete flag
  * @param created created date time
  * @param updated updated date time
  */
case class Tenant(
    id: UUID,
    name: String,
    explain: Option[String],
    delete_flag: Boolean,
    created: DateTime,
    updated: Option[DateTime]
)
