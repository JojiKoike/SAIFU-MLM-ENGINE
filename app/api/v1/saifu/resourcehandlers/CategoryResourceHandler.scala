package api.v1.saifu.resourcehandlers

import api.v1.saifu.models.{
  CreateMainCategoryInput,
  CreateSubCategoryInput,
  UpdateMainCategoryInput,
  UpdateSubCategoryInput
}
import com.saifu_mlm.engine.saifu.{SaifuMainCategory, SaifuMainCategoryDAO, SaifuSubCategory, SaifuSubCategoryDAO}
import javax.inject.Inject
import play.api.MarkerContext
import play.api.libs.json.{Format, Json}

import scala.concurrent.{ExecutionContext, Future}

case class SaifuCategoryResource(mainCategory: SaifuMainCategoryResource, subCategories: Seq[SaifuSubCategoryResource])

object SaifuCategoryResource {
  implicit val format: Format[SaifuCategoryResource] = Json.format
}

case class SaifuMainCategoryResource(id: String, name: String, explain: Option[String])

object SaifuMainCategoryResource {
  implicit val format: Format[SaifuMainCategoryResource] = Json.format
}

case class SaifuSubCategoryResource(id: String, mainCategoryID: String, name: String, explain: Option[String])

object SaifuSubCategoryResource {
  implicit val format: Format[SaifuSubCategoryResource] = Json.format
}

/**
  * Saifu Category Data Resource Handler
  * @param saifuMainCategoryDAO Saifu Main Category DAO
  * @param saifuSubcategoryDAO Saifu Sub Category DAO
  * @param ec ExecutionContext
  */
class CategoryResourceHandler @Inject() (
    saifuMainCategoryDAO: SaifuMainCategoryDAO,
    saifuSubcategoryDAO: SaifuSubCategoryDAO
)(implicit ec: ExecutionContext) {

  /**
    * Lookup Specified Main Category and it's Sub Category belongs to Specified Tenant
    * @param tenantID Tenant ID
    * @param id Main Category ID
    * @param mc MarkerContext
    * @return
    */
  def lookup(tenantID: String, id: String)(implicit mc: MarkerContext): Future[Option[SaifuCategoryResource]] = {
    // Select Query Specified Saifu Main Category belongs to Specified Tenant
    val mainCategory = saifuMainCategoryDAO
      .lookUp(tenantID, id)
      .map(mayBeMainCategoryData => mayBeMainCategoryData.map(mainCategoryToResource))
    // Select Query Saifu Sub Category belongs to Specified Saifu Main Category and Tenant
    val subCategory = saifuSubcategoryDAO
      .allOfOneMainCategory(tenantID, id)
      .map(subCategories => subCategories.map(subCategoryToResource))
    // Execute Queries Parallel
    (mainCategory zipWith subCategory)((main, sub) => main.map(item => SaifuCategoryResource(item, sub)))
  }

  /**
    * Get All Saifu Category Data belongs to Specified Tenant
    * @param tenantID Tenant ID
    * @param mc MarkerContext
    * @return
    */
  def all(tenantID: String)(implicit mc: MarkerContext): Future[Iterable[SaifuCategoryResource]] = {
    // Select Query All Saifu Main Category belongs to Specified Tenant
    val mainCategories = saifuMainCategoryDAO
      .all(tenantID)
      .map(mainCategoryList => mainCategoryList.map(mainCategoryToResource))
    // Select Query All Saifu Sub Category belongs to Specified Tenant
    val subCategories = saifuSubcategoryDAO
      .all(tenantID)
      .map(subCategoryList => subCategoryList.map(subCategoryToResource))
    // Execute Queries Parallel
    (mainCategories zipWith subCategories)((main, sub) => {
      main.map(itemMain => SaifuCategoryResource(itemMain, sub.filter(_.id == itemMain.id)))
    })
  }

  /**
    * Create Saifu Main Category
    * @param tenantID Tenant ID
    * @param createMainInput Form Input Data
    * @param mc MarkerContext
    * @return
    */
  def createMain(tenantID: String, createMainInput: CreateMainCategoryInput)(implicit
      mc: MarkerContext
  ): Future[Int] = {
    saifuMainCategoryDAO.create(
      SaifuMainCategory(
        tenantID = tenantID,
        name = createMainInput.name,
        explain = createMainInput.explain
      )
    )
  }

  /**
    * Update Saifu Main Category
    * @param tenantID Tenant ID
    * @param updateMainInput Form Input Data
    * @param mc MarkerContext
    * @return
    */
  def updateMain(tenantID: String, updateMainInput: UpdateMainCategoryInput)(implicit
      mc: MarkerContext
  ): Future[Int] = {
    saifuMainCategoryDAO.update(
      SaifuMainCategory(
        updateMainInput.id,
        tenantID,
        updateMainInput.name,
        updateMainInput.explain
      )
    )
  }

  /**
    * Create Saifu Sub Category
    * @param tenantID Tenant ID
    * @param createSubInput Form Input Data
    * @param mc MarkerContext
    * @return
    */
  def createSub(tenantID: String, createSubInput: CreateSubCategoryInput)(implicit mc: MarkerContext): Future[Int] = {
    saifuSubcategoryDAO.create(
      SaifuSubCategory(
        saifuMainCategoryID = createSubInput.mainCategoryID,
        tenantID = tenantID,
        name = createSubInput.name,
        explain = createSubInput.explain
      )
    )
  }

  /**
    * Update Saifu Sub Category
    * @param tenantID Tenant ID
    * @param updateSubInput Form Input Data
    * @param mc MarkerContext
    * @return
    */
  def updateSub(tenantID: String, updateSubInput: UpdateSubCategoryInput)(implicit mc: MarkerContext): Future[Int] = {
    saifuSubcategoryDAO.update(
      SaifuSubCategory(
        updateSubInput.id,
        updateSubInput.mainCategoryID,
        tenantID,
        updateSubInput.name,
        updateSubInput.explain
      )
    )
  }

  /**
    * Saifu Main Category DAO Format -> Resource Format Converter
    * @param mainCategory SaifuMainCategory (DAO Format)
    * @return
    */
  private def mainCategoryToResource(mainCategory: SaifuMainCategory): SaifuMainCategoryResource = {
    SaifuMainCategoryResource(
      mainCategory.id,
      mainCategory.name,
      Option(mainCategory.explain)
    )
  }

  /**
    * Saifu Sub Category DAO Format -> Resource Format Converter
    * @param subCategory SaifuSubCategory (DAO Format)
    * @return
    */
  private def subCategoryToResource(subCategory: SaifuSubCategory): SaifuSubCategoryResource = {
    SaifuSubCategoryResource(
      subCategory.id,
      subCategory.saifuMainCategoryID,
      subCategory.name,
      Option(subCategory.explain)
    )
  }
}
