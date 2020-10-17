package com.saifu_mlm.engine.account.slick

import java.util.UUID

import com.saifu_mlm.engine.account.{User, UserDAO}
import com.saifu_mlm.engine.common.string2UUID
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickUserDAO @Inject() (db: Database)(implicit ec: ExecutionContext) extends UserDAO with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryById = (id: UUID) => MUsers.filter(!_.deleteFlag).filter(_.id === id)

  override def create(user: User): Future[Int] = {
    db.run(
      (MUsers.map(item => (item.tenantId, item.roleId, item.loginId, item.name, item.password, item.`e-mail`))
        += (Option(string2UUID(user.tenantId)),
          Option(string2UUID(user.roleId)),
          user.loginId, user.name, user.password, user.eMail)).transactionally
    )
  }

  override def all: Future[Seq[User]] = {
    db.run(
        MUsers
          .filter(!_.deleteFlag)
          .result
      )
      .map(items => items.map(mUsersRowToUser))
  }

  override def lookup(id: String): Future[Option[User]] = {
    db.run(
        queryById(string2UUID(id)).result.headOption
      )
      .map(maybeRow => maybeRow.map(mUsersRowToUser))
  }

  override def update(user: User): Future[Int] = {
    db.run(
      queryById(string2UUID(user.id))
        .map(target => (target.tenantId, target.roleId, target.loginId, target.name, target.password, target.`e-mail`))
        .update(
          Option(string2UUID(user.tenantId)),
          Option(string2UUID(user.roleId)),
          user.loginId,
          user.name,
          user.password,
          user.eMail
        )
    )
  }

  override def delete(id: String): Future[Int] = {
    db.run(
      queryById(string2UUID(id))
        .map(_.deleteFlag)
        .update(true)
        .transactionally
    )
  }

  override def login(loginID: String): Future[Option[User]] = {
    db.run(
        MUsers
          .filter(!_.deleteFlag)
          .filter(_.loginId === loginID)
          .result
          .headOption
      )
      .map(mayBeRow => mayBeRow.map(mUsersRowToUser))
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

  /**
    * Convert MUsersRow -> User
    * @param mUsersRow MUsersRow
    * @return User
    */
  private def mUsersRowToUser(mUsersRow: MUsersRow): User = {
    User(
      mUsersRow.id.toString,
      mUsersRow.tenantId.toString,
      mUsersRow.roleId.toString,
      mUsersRow.loginId,
      mUsersRow.name,
      mUsersRow.password,
      mUsersRow.`e-mail`
    )
  }

}
