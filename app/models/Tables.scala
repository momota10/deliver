package models

import slick.driver.MySQLDriver.api._
import slick.jdbc.{GetResult => GR}
import slick.model.ForeignKeyAction


object Tables extends {
  val profile = slick.driver.MySQLDriver
} with Tables_trait


trait Tables_trait {

  lazy val schema = Genres.schema ++ Campaigns.schema
  // def ddl = schema //不要？

  /** Entity class storing rows of table Genres
   *  @param id
   *  @param name
   */
  case class GenresRow(id: Int, name: String)

  implicit def GetResultGenresRow(implicit e0: GR[Int], e1: GR[String]): GR[GenresRow] = GR{
    prs => import prs._
    GenresRow.tupled((<<[Int], <<[String]))
  }

  /** Table description of table GENRES. Objects of this class serve as prototypes for rows in queries. */
  class Genres(_tableTag: Tag) extends Table[GenresRow](_tableTag, "GENRES") {
    def * = (id, name) <> (GenresRow.tupled, GenresRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name)).shaped.<>({r=>import r._; _1.map(_=> GenresRow.tupled((_1.get, _2.get)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(INTEGER), PrimaryKey */
    val id: Rep[Int] = column[Int]("ID", O.PrimaryKey)
    /** Database column NAME SqlType(VARCHAR) */
    val name: Rep[String] = column[String]("NAME")
  }
  /** Collection-like TableQuery object for table Genres */
  lazy val Genres = new TableQuery(tag => new Genres(tag))






  /** Entity class storing rows of table Campaigns
   *  @param id Database column ID SqlType(BIGINT), AutoInc, PrimaryKey
   *  @param name Database column NAME SqlType(VARCHAR)
   *  @param genreId Database column GENRE_ID SqlType(INTEGER)
   *  @param count
   */

  case class CampaignsRow(id: Long, name: String, genreId: Option[Int], count: Int)

  /** GetResult implicit for fetching CampaignsRow objects using plain SQL queries */
  implicit def GetResultCampaignsRow(implicit e0: GR[Long], e1: GR[String], e2: GR[Option[Int]], e3: GR[Int]): GR[CampaignsRow] = GR{
    prs => import prs._
    CampaignsRow.tupled((<<[Long], <<[String], <<?[Int], <<[Int]))
  }


  /** Table description of table CAMPAIGNS. Objects of this class serve as prototypes for rows in queries. */
  class Campaigns(_tableTag: Tag) extends Table[CampaignsRow](_tableTag, "CAMPAIGNS") {
    def * = (id, name, genreId, count) <> (CampaignsRow.tupled, CampaignsRow.unapply)
    /** Maps whole row to an option. Useful for outer joins. */
    def ? = (Rep.Some(id), Rep.Some(name), genreId, count).shaped.<>({r=>import r._; _1.map(_=> CampaignsRow.tupled((_1.get, _2.get, _3, _4)))}, (_:Any) =>  throw new Exception("Inserting into ? projection not supported."))

    /** Database column ID SqlType(BIGINT), AutoInc, PrimaryKey */
    val id: Rep[Long] = column[Long]("ID", O.AutoInc, O.PrimaryKey)
    /** Database column NAME SqlType(VARCHAR) */
    val name: Rep[String] = column[String]("NAME")
    /** Database column GENRE_ID SqlType(INTEGER) */
    val genreId: Rep[Option[Int]] = column[Option[Int]]("GENRE_ID")
    val count: Rep[Int] = column[Int]("COUNT")

    /** Foreign key referencing Genres (database name IDX_CAMPAINS_FK0) */
    // lazy val genresFk = foreignKey("IDX_CAMPAINS_FK0", genreId, Genres)(r => Rep.Some(r.id), onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Restrict)
  }
  /** Collection-like TableQuery object for table Campaigns */
  //クラスのインスタンス化
  lazy val Campaigns = new TableQuery(tag => new Campaigns(tag))



}
