package com.saifu_mlm.engine.account.slick

import java.util.UUID

import com.saifu_mlm.engine.account.{User, UserDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickUserDAO @Inject() (db: Database)(implicit ec: ExecutionContext) extends UserDAO with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryById = Compiled((id: Rep[UUID]) => MUsers.filter(_.id === id).filter(!_.deleteFlag))

  override def create(user: User): Future[Int] = {
    db.run(
      MUsers += userToUsersRow(user)
          .copy(created = DateTime.now())
    )
  }

  override def all: Future[Seq[User]] = {
    val f = db.run(Compiled(MUsers.filter(!_.deleteFlag)).result)
    f.map(seq => seq.map(mUsersRowToUser))
  }

  override def lookup(id: String): Future[Option[User]] = {
    val f: Future[Option[MUsersRow]] =
      db.run(queryById(UUID.fromString(id)).result.headOption)
    f.map(maybeRow => maybeRow.map(mUsersRowToUser))
  }

  override def update(user: User): Future[Int] = {
    db.run(
      queryById(UUID.fromString(user.id.get))
        .update(
          userToUsersRow(user)
            .copy(updated = Option(DateTime.now()))
        )
    )
  }

  override def delete(id: String): Future[Int] = {
    db.run(
      queryById(UUID.fromString(id)).delete
    )
  }

  override def login(loginID: String): Future[Option[User]] = {
    val f: Future[Option[MUsersRow]] = {
      db.run(
        Compiled(
          MUsers
            .filter(_.loginId === loginID)
            .filter(!_.deleteFlag)
        ).result.headOption
      )
    }
    f.map(mayBeRow => mayBeRow.map(mUsersRowToUser))
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

  /**
    * Convert User -> MUsersRow
    * @param user User
    * @return MUsersRow
    */
  private def userToUsersRow(user: User): MUsersRow =
    MUsersRow(
      UUID.fromString(user.id.getOrElse(UUID.randomUUID().toString)),
      Option(UUID.fromString(user.tenantId)),
      Option(UUID.fromString(user.roleId)),
      user.loginId,
      user.name,
      user.password,
      user.eMail,
      deleteFlag = false,
      user.created.getOrElse(DateTime.now()), // Set Dummy Datetime if Create
      user.updated
    )

  /**
    * Convert MUsersRow -> User
    * @param mUsersRow MUsersRow
    * @return User
    */
  private def mUsersRowToUser(mUsersRow: MUsersRow): User = {
    User(
      Option(mUsersRow.id.toString),
      mUsersRow.tenantId.toString,
      mUsersRow.roleId.toString,
      mUsersRow.loginId,
      mUsersRow.name,
      mUsersRow.password,
      mUsersRow.`e-mail`,
      Option(mUsersRow.created),
      mUsersRow.updated
    )
  }

}
