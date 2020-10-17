package com.saifu_mlm.engine.account.slick

import java.util.UUID

import com.saifu_mlm.engine.account.{Tenant, TenantDAO}
import com.saifu_mlm.engine.common.string2UUID
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickTenantDAO @Inject() (db: Database)(implicit ec: ExecutionContext) extends TenantDAO with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryById = (id: Rep[UUID]) => MTenants.filter(!_.deleteFlag).filter(_.id === id)

  override def lookUp(id: String): Future[Option[Tenant]] = {
    db.run(queryById(string2UUID(id)).result.headOption)
      .map(maybeRow => maybeRow.map(mTenantsRowToTenant))
  }

  override def all: Future[Seq[Tenant]] = {
    db.run(MTenants.filter(!_.deleteFlag).result)
      .map(results => results.map(mTenantsRowToTenant))
  }

  override def update(tenant: Tenant): Future[Int] = {
    db.run(
      queryById(string2UUID(tenant.id))
        .map(target => (target.name, target.explain))
        .update(tenant.name, tenant.explain)
        .transactionally
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

  override def create(tenant: Tenant): Future[Tenant] = {
    db.run(
      (MTenants
        .map(item => (item.name, item.explain))
        += (tenant.name, tenant.explain))
        .andThen(
          MTenants
            .filter(_.name === tenant.name)
            .result
            .head
        )
        .transactionally
        .map(mTenantsRowToTenant)
    )
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

  private def mTenantsRowToTenant(mTenantsRow: MTenantsRow): Tenant = {
    Tenant(
      mTenantsRow.id.toString,
      mTenantsRow.name,
      mTenantsRow.explain
    )
  }
}
