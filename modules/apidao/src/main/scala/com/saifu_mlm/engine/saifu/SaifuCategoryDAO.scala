package com.saifu_mlm.engine.saifu

import scala.concurrent.Future

trait SaifuMainCategoryDAO extends SaifuCategoryDAOBase[SaifuMainCategory]

trait SaifuSubCategoryDAO extends SaifuCategoryDAOBase[SaifuSubCategory] {

  /**
    * Select Query by tenantID and MainCategoryID
    * @param tenantID Owner Tenant ID
    * @param saifuMainCategoryID Main Category ID
    * @return
    */
  def allOfOneMainCategory(tenantID: String, saifuMainCategoryID: String): Future[Seq[SaifuSubCategory]]

}

/**
  * Saifu Category DAO Base Trait
  * @tparam A Target Object Type
  */
trait SaifuCategoryDAOBase[A] {

  /**
    * Select Query by tenantID and id
    * @param tenantID tenantID
    * @param id id
    * @return
    */
  def lookUp(tenantID: String, id: String): Future[Option[A]]

  /**
    * Select Query by tenantID
    * @param tenantID tenantID
    * @return
    */
  def all(tenantID: String): Future[Seq[A]]

  /**
    * Insert Query
    * @param item Insert item
    * @return
    */
  def create(item: A): Future[Int]

  /**
    * Update Query
    * @param item Update item
    * @return
    */
  def update(item: A): Future[Int]

  def close(): Future[Unit]
}

case class SaifuMainCategory(id: String, tenantID: String, name: String, explain: String)

case class SaifuSubCategory(
    id: String,
    saifuMainCategoryID: String,
    tenantID: String,
    name: String,
    explain: String
)
