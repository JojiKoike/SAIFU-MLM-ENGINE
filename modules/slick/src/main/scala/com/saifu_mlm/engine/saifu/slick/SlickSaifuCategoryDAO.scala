package com.saifu_mlm.engine.saifu.slick

import java.util.UUID

import com.saifu_mlm.engine.common.string2UUID
import com.saifu_mlm.engine.saifu.{SaifuMainCategory, SaifuMainCategoryDAO, SaifuSubCategory, SaifuSubCategoryDAO}
import com.saifu_mlm.infrastructure.db.slick.Tables
import javax.inject.{Inject, Singleton}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.JdbcProfile

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SlickSaifuMainCategoryDAO @Inject() (db: Database)(implicit ec: ExecutionContext)
    extends SaifuMainCategoryDAO
    with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryByTenantID = (tenantID: Rep[UUID]) =>
    MSaifuMainCategories
      .filter(!_.deleteFlag)
      .filter(_.tenantId === tenantID)

  override def lookUp(tenantID: String, id: String): Future[Option[SaifuMainCategory]] = {
    db.run(
        queryByTenantID(string2UUID(tenantID))
          .filter(_.id === id.toInt)
          .result
          .headOption
      )
      .map(mayBeRow => mayBeRow.map(rowToCase))
  }

  override def all(tenantID: String): Future[Seq[SaifuMainCategory]] = {
    db.run(
        queryByTenantID(string2UUID(tenantID)).result
      )
      .map(item => item.map(rowToCase))
  }

  override def create(saifuMainCategory: SaifuMainCategory): Future[Int] = {
    db.run(
      (MSaifuMainCategories
        .map(target => (target.tenantId, target.name, target.explain))
        += (Option(string2UUID(saifuMainCategory.tenantID)),
          saifuMainCategory.name, Option(saifuMainCategory.explain))).transactionally
    )
  }

  override def update(saifuMainCategory: SaifuMainCategory): Future[Int] = {
    db.run(
      queryByTenantID(string2UUID(saifuMainCategory.tenantID))
        .filter(_.id === saifuMainCategory.id.toInt)
        .map(target => (target.name, target.explain))
        .update((saifuMainCategory.name, Option(saifuMainCategory.explain)))
        .transactionally
    )
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

  private def rowToCase: MSaifuMainCategoriesRow => SaifuMainCategory =
    row => SaifuMainCategory(row.id.toString, row.tenantId.toString, row.name, row.explain.getOrElse(""))

}

@Singleton
class SlickSaifuSubCategoryDAO @Inject() (db: Database)(implicit ec: ExecutionContext)
    extends SaifuSubCategoryDAO
    with Tables {

  override val profile: JdbcProfile = _root_.slick.jdbc.PostgresProfile

  import profile.api._

  private val queryByTenantID =
    (id: Rep[UUID]) => MSaifuSubCategories.filter(!_.deleteFlag).filter(_.tenantId === id)

  override def lookUp(id: String, tenantID: String): Future[Option[SaifuSubCategory]] = {
    db.run(
        queryByTenantID(string2UUID(tenantID))
          .filter(_.id === id.toInt)
          .result
          .headOption
      )
      .map(maybeRow => maybeRow.map(rowToCase))
  }

  override def all(tenantID: String): Future[Seq[SaifuSubCategory]] = {
    db.run(
        queryByTenantID(string2UUID(tenantID)).result
      )
      .map(results => results.map(rowToCase))
  }

  override def allOfOneMainCategory(tenantID: String, saifuMainCategoryID: String): Future[Seq[SaifuSubCategory]] = {
    db.run(
        queryByTenantID(string2UUID(tenantID))
          .filter(_.saifuMainCategoryId === saifuMainCategoryID.toInt)
          .result
      )
      .map(results => results.map(rowToCase))
  }

  override def create(saifuSubCategory: SaifuSubCategory): Future[Int] = {
    db.run(
      (MSaifuSubCategories
        .map(item => (item.saifuMainCategoryId, item.tenantId, item.name, item.explain)) +=
        (Option(saifuSubCategory.saifuMainCategoryID.toInt),
        Option(string2UUID(saifuSubCategory.tenantID)),
        saifuSubCategory.name,
        Option(saifuSubCategory.explain))).transactionally
    )
  }

  override def update(saifuSubCategory: SaifuSubCategory): Future[Int] = {
    db.run(
      queryByTenantID(string2UUID(saifuSubCategory.tenantID))
        .filter(_.saifuMainCategoryId === saifuSubCategory.saifuMainCategoryID.toInt)
        .filter(_.id === saifuSubCategory.id.toInt)
        .map(target => (target.name, target.explain))
        .update((saifuSubCategory.name, Option(saifuSubCategory.explain)))
        .transactionally
    )
  }

  override def close(): Future[Unit] = {
    Future.successful(db.close())
  }

  private def rowToCase: MSaifuSubCategoriesRow => SaifuSubCategory =
    (row: MSaifuSubCategoriesRow) =>
      SaifuSubCategory(
        row.id.toString,
        row.saifuMainCategoryId.toString,
        row.tenantId.toString,
        row.name,
        row.explain.getOrElse("")
      )

}
