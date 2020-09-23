package com.saifu_mlm.engine.account.slick

import java.util.UUID

import com.saifu_mlm.engine.account.{Role, RoleDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickRoleDAO @Inject() (db: Database)(implicit ec: ExecutionContext) extends RoleDAO with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryById = Compiled((id: Rep[UUID]) => MRoles.filter(_.id === id).filter(!_.deleteFlag))

  override def lookUp(id: String): Future[Option[Role]] = {
    val f: Future[Option[MRolesRow]] =
      db.run(queryById(UUID.fromString(id)).result.headOption)
    f.map(maybeRow => maybeRow.map(mRolesRowToRole))
  }

  override def all: Future[Seq[Role]] = {
    val f = db.run(Compiled(MRoles.filter(!_.deleteFlag)).result)
    f.map(seq => seq.map(mRolesRowToRole))
  }

  override def update(role: Role): Future[Int] = {
    db.run(
      queryById(role.id)
        .update(roleToRolesRow(role.copy(updated = Option(DateTime.now()))))
    )
  }

  override def delete(id: String): Future[Int] = {
    db.run(
      queryById(UUID.fromString(id)).delete
    )
  }

  override def create(role: Role): Future[Int] = {
    db.run(
      MRoles += roleToRolesRow(role.copy(created = DateTime.now()))
    )
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

  private def roleToRolesRow(role: Role): MRolesRow = {
    MRolesRow(role.id, role.name, role.explain, role.delete_flag, role.created, role.updated)
  }

  private def mRolesRowToRole(mRolesRow: MRolesRow): Role = {
    Role(
      mRolesRow.id,
      mRolesRow.name,
      mRolesRow.explain,
      mRolesRow.deleteFlag,
      mRolesRow.created,
      mRolesRow.updated
    )
  }

}
