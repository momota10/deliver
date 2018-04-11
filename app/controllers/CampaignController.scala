package controllers

import play.api.mvc._
import play.api.data._
import play.api.data.Forms._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.i18n.{MessagesApi, I18nSupport}
import play.api.db.slick._

import slick.driver.JdbcProfile
import models.Tables._
import javax.inject.Inject
import scala.concurrent.Future

import slick.driver.MySQLDriver.api._
// import slick.driver.H2Driver.api._

// コンパニオンオブジェクトに定義したFormを参照するためにimport文を追加
import CampaignController._


//コンパニオンオブジェクトの定義
object CampaignController {
  // フォームの値を格納するケースクラス（コンパニオンオブジェクト内で定義するのが一般的になっている）
  case class CampaignForm(id: Option[Long], name: String, genreId: Option[Int], count: Int)

  // formから送信されたデータ ⇔ ケースクラスの変換を行う
  val campaignForm = Form(
    mapping(
      "id"        -> optional(longNumber),
      "name"      -> nonEmptyText(maxLength = 20),
      "genreId" -> optional(number),
      "count"     -> number
    // )
    )(CampaignForm.apply)(CampaignForm.unapply)
  )
}


class CampaignController @Inject()(val dbConfigProvider: DatabaseConfigProvider,
                               val messagesApi: MessagesApi) extends Controller
    with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

  /**
   * 一覧表示
   */
  def list = Action.async { implicit rs =>
    // IDの昇順にすべてのユーザ情報を取得
    // Campaigns.sortBy(t => t.id).result = SELECT * FROM CAMPAIGNS ORDER BY ID
    db.run(Campaigns.sortBy(campaign => campaign.id).result).map { campaigns =>
      // 一覧画面を表示
      Ok(views.html.campaign.list(campaigns))
    }
  }

  /**
   * 編集画面表示
   */
  //多分implicitは不要。一応エラーがあったときのために
  // def edit(id: Option[Long]) = Action.async { implicit rs =>
  def edit(id: Option[Long]) = Action.async {
    // リクエストパラメータにIDが存在する場合
    val form = if(id.isDefined) {
      // IDからユーザ情報を1件取得
      db.run(Campaigns.filter(t => t.id === id.get.bind).result.head).map { campaign =>
        // 値をフォームに詰める
        campaignForm.fill(CampaignForm(Some(campaign.id), campaign.name, campaign.genreId, campaign.count))
      }
    } else {
      // リクエストパラメータにIDが存在しない場合
      //このアクションもAction.asyncでFutureを返しますが、
      //リクエストパラメータにIDが存在しない場合は以下のようにして自分でcampaignFormを返すFutureを作成している点に注意してください。
      Future { campaignForm }
    }

    form.flatMap { form =>
      // 会社一覧を取得
      // Genres.sortBy(_.id).result = SELECT * FROM COMPANIES ORDER BY ID
      db.run(Genres.sortBy(_.id).result).map { genres =>
        Ok(views.html.campaign.edit(form, genres))
      }
    }

  }

  /**
   * 登録実行
   */
  //
  def create = Action.async { implicit request =>
    // リクエストの内容をバインド
    //bindFromRequest：play2.xの機能
    campaignForm.bindFromRequest.fold(
      // エラーの場合
      error => {
        db.run(Genres.sortBy(t => t.id).result).map { genres =>
          BadRequest(views.html.campaign.edit(error, genres))
        }
      },
      // OKの場合
      form  => {
        // ユーザを登録
        val campaign = CampaignsRow(0, form.name, form.genreId, 0)
        //Campaigns += campaign = INSERT INTO CAMPAIGNS (ID, NAME, COMPANY_ID) VALUES (?, ?, ?)
        db.run(Campaigns += campaign).map { _ =>
          // 一覧画面へリダイレクト
          Redirect(routes.CampaignController.list)
        }
      }
    )
  }

  /**
   * 更新実行
   */
  def update = Action.async { implicit rs =>
    // リクエストの内容をバインド
    campaignForm.bindFromRequest.fold(
      // エラーの場合は登録画面に戻す
      error => {
        db.run(Genres.sortBy(t => t.id).result).map { genres =>
          BadRequest(views.html.campaign.edit(error, genres))
        }
      },
      // OKの場合は登録を行い一覧画面にリダイレクトする
      form  => {
        // ユーザ情報を更新
        val campaign = CampaignsRow(form.id.get, form.name, form.genreId, form.count)
        //Campaigns.filter(t => t.id === campaign.id.bind).update(campaign) = UPDATE CAMPAIGNS SET NAME = ?, COMPANY_ID = ? WHERE ID = ?
        db.run(Campaigns.filter(t => t.id === campaign.id.bind).update(campaign)).map { _ =>
          // 一覧画面にリダイレクト
          Redirect(routes.CampaignController.list)
        }
      }
    )
  }

  /**
   * 削除実行
   */
  // def remove(id: Long) = TODO
  def remove(id: Long) = Action.async { implicit rs =>
    // ユーザを削除
    //Campaigns.filter(t => t.id === id.bind).delete = DELETE FROM CAMPAIGNS WHERE ID = ?
    db.run(Campaigns.filter(t => t.id === id.bind).delete).map { _ =>
      // 一覧画面へリダイレクト
      Redirect(routes.CampaignController.list)
    }
  }

}
