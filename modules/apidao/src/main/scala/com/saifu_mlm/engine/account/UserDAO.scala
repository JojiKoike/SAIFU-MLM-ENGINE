package com.saifu_mlm.engine.account

import scala.concurrent.Future

/**
  * UserDAO : implemented by Slick DAO
  */
trait UserDAO {

  def create(userInput: User): Future[Int]

  def all: Future[Seq[User]]

  def lookup(id: String): Future[Option[User]]

  def update(userInput: User): Future[Int]

  def delete(id: String): Future[Int]

  def login(loginID: String): Future[Option[User]]

  def close(): Future[Unit]

}

/**
  * UserInput Entity Class
  * @param id - User Id
  * @param tenantId - Tenant Id
  * @param roleId - Role ID
  * @param loginId - Login ID
  * @param name - User Name
  * @param password - Login Password
  * @param eMail - E-Mail
  */
case class User(
    id: String,
    tenantId: String,
    roleId: String,
    loginId: String,
    name: String,
    password: String,
    eMail: String
)
