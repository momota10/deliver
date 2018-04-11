package controllers

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.db.slick._

//play.api.libs.json._：Play2のJSONサポート機能を使用するために必要なimport文
import play.api.libs.json._
import play.api.libs.functional.syntax._

import slick.driver.JdbcProfile
import slick.driver.MySQLDriver.api._
// import slick.driver.H2Driver.api._

import models.Tables._
import javax.inject.Inject
import scala.concurrent.Future

// コンパニオンオブジェクトに定義したReads、Writesを参照するためにimport文を追加
import JsonController._



//コンパニオンオブジェクト
//https://github.com/bizreach/play2-hands-on/blob/master/play2.4-slick3.0/markdown/10_implement_list_api.md
object JsonController {
  //CampaignRowをJSONに変換するためのWriteを定義
  implicit val campaignsRowWritesWrites = (
    (__ \ "id"       ).write[Long]   and
    (__ \ "name"     ).write[String] and
    (__ \ "genreId").writeNullable[Int] and
    (__ \ "count").write[Int]
  )(unlift(CampaignsRow.unapply))
}



class JsonController @Inject()(val dbConfigProvider: DatabaseConfigProvider) extends Controller
    with HasDatabaseConfigProvider[JdbcProfile] {

  /**
   * 一覧表示
   */
  def index(id: Option[Long]) = Action.async { implicit rs =>

    if(id.isDefined) {

      // db.run(Campaigns.filter(t => t.id === id.get.bind).update(Campaigns.count(10)))
      // db.run(Campaigns.filter(t => t.id === id.get.bind).map(_.count).update(10)) //成功

      val q = Campaigns.filter(t => t.id === id.get.bind).map(_.count)

      db.run(Campaigns.filter(t => t.id === id.get.bind).result.head).map { campaign =>
        // println(campaign.count)
        db.run(q.update(campaign.count + 1))
      }


      db.run(Campaigns.filter(t => t.id === id.get.bind).result.head).map { campaigns =>
        Ok(Json.obj("campaigns" -> campaigns))
      }
    } else {
      // IDの昇順にすべてのユーザ情報を取得
      db.run(Campaigns.sortBy(t => t.id).result).map { campaigns =>
        // ユーザの一覧をJSONで返す
        Ok(Json.obj("campaigns" -> campaigns))
        // Ok(views.html.json(Json.obj("campaigns" -> campaigns)))
      }
    }
  }


}
