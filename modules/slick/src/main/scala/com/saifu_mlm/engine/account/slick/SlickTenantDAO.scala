package com.saifu_mlm.engine.account.slick

import java.util.UUID

import com.saifu_mlm.engine.account.{Tenant, TenantDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import org.joda.time.DateTime
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickTenantDAO @Inject() (db: Database)(implicit ec: ExecutionContext) extends TenantDAO with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryById = Compiled((id: Rep[UUID]) => MTenants.filter(_.id === id).filter(!_.deleteFlag))

  override def lookUp(id: String): Future[Option[Tenant]] = {
    val f: Future[Option[MTenantsRow]] =
      db.run(queryById(UUID.fromString(id)).result.headOption)
    f.map(maybeRow => maybeRow.map(mTenantsRowToTenant))
  }

  override def all: Future[Seq[Tenant]] = {
    val f = db.run(MTenants.result)
    f.map(seq => seq.map(mTenantsRowToTenant))
  }

  override def update(tenant: Tenant): Future[Int] = {
    db.run(queryById(tenant.id).update(tenantToMTenantsRow(tenant.copy(updated = Option(DateTime.now())))))
  }

  override def delete(id: String): Future[Int] = {
    db.run(queryById(UUID.fromString(id)).delete)
  }

  override def create(tenant: Tenant): Future[Int] = {
    db.run(
      MTenants += tenantToMTenantsRow(tenant.copy(created = DateTime.now()))
    )
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

  private def tenantToMTenantsRow(tenant: Tenant): MTenantsRow = {
    MTenantsRow(tenant.id, tenant.name, tenant.explain, tenant.delete_flag, tenant.created, tenant.updated)
  }

  private def mTenantsRowToTenant(mTenantsRow: MTenantsRow): Tenant = {
    Tenant(
      mTenantsRow.id,
      mTenantsRow.name,
      mTenantsRow.explain,
      mTenantsRow.deleteFlag,
      mTenantsRow.created,
      mTenantsRow.updated
    )
  }
}
