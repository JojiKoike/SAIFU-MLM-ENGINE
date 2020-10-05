package com.saifu_mlm.engine.saifu

import scala.concurrent.Future

/**
  * Saifu DAO Trait
  */
trait SaifuDAO {

  def lookup(userID: String, id: String): Future[Option[Saifu]]

  def all(userID: String): Future[Seq[Saifu]]

  def create(saifu: Saifu): Future[Any]

  def update(saifu: Saifu): Future[Any]

  def close(): Future[Unit]

}

/**
  * Saifu Entity
  * @param id
  * @param subCategoryID
  * @param userID
  * @param name
  * @param explain
  * @param currentBalance
  */
case class Saifu(
    id: String = "",
    subCategoryID: String,
    userID: String,
    name: String,
    explain: String = "",
    currentBalance: BigInt = 0
)
