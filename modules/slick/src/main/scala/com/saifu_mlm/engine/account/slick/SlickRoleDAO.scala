package com.saifu_mlm.engine.account.slick

import java.util.UUID

import com.saifu_mlm.engine.account.{Role, RoleDAO}
import com.saifu_mlm.engine.common.string2UUID
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickRoleDAO @Inject() (db: Database)(implicit ec: ExecutionContext) extends RoleDAO with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryById = (id: Rep[UUID]) => MRoles.filter(_.id === id).filter(!_.deleteFlag)

  override def lookUp(id: String): Future[Option[Role]] = {
    db.run(queryById(string2UUID(id)).result.headOption)
      .map(maybeRow => maybeRow.map(mRolesRowToRole))
  }

  override def all: Future[Seq[Role]] = {
    db.run(MRoles.filter(!_.deleteFlag).result)
      .map(results => results.map(mRolesRowToRole))
  }

  override def update(role: Role): Future[Int] = {
    db.run(
      queryById(string2UUID(role.id))
        .map(target => (target.name, target.explain))
        .update(role.name, role.explain)
    )
  }

  override def delete(id: String): Future[Int] = {
    db.run(
      queryById(string2UUID(id))
        .map(target => target.deleteFlag)
        .update(true)
        .transactionally
    )
  }

  override def create(role: Role): Future[Role] = {
    db.run(
      (MRoles
        .map(item => (item.name, item.explain))
        += (role.name, role.explain))
        .andThen(
          MRoles
            .filter(_.name === role.name)
            .result
            .head
        )
        .transactionally
        .map(mRolesRowToRole)
    )
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

  private def mRolesRowToRole(mRolesRow: MRolesRow): Role = {
    Role(
      mRolesRow.id.toString,
      mRolesRow.name,
      mRolesRow.explain
    )
  }

}
