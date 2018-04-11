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
import GenreController._


//コンパニオンオブジェクトの定義
object GenreController {
  // フォームの値を格納するケースクラス（コンパニオンオブジェクト内で定義するのが一般的になっている）
  case class GenreForm(id: Option[Int], name: String)

  // formから送信されたデータ ⇔ ケースクラスの変換を行う
  val genreForm = Form(
    mapping(
      "id"        -> optional(number),
      "name"      -> nonEmptyText(maxLength = 20)
    )(GenreForm.apply)(GenreForm.unapply)
  )
}


class GenreController @Inject()(val dbConfigProvider: DatabaseConfigProvider,
                               val messagesApi: MessagesApi) extends Controller
    with HasDatabaseConfigProvider[JdbcProfile] with I18nSupport {

  /**
   * 一覧表示
   */
   def list = Action.async { implicit rs =>
     // IDの昇順にすべてのユーザ情報を取得
     // Users.sortBy(t => t.id).result = SELECT * FROM USERS ORDER BY ID
     db.run(Genres.sortBy(genre => genre.id).result).map { genres =>
       // 一覧画面を表示
       Ok(views.html.genre.list(genres))
     }
   }

  /**
   * 編集画面表示
   */
  //  def edit(id: Option[Long]) = TODO
  def edit(id: Option[Int]) = Action.async { implicit rs =>
    val form = if(id.isDefined) {
      db.run(Genres.filter(t => t.id === id.get.bind).result.head).map { genre =>
        genreForm.fill(GenreForm(Some(genre.id), genre.name))
      }
    } else {
      Future { genreForm }
    }
    form.flatMap { form =>
      db.run(Genres.sortBy(_.id).result).map { genres =>
        Ok(views.html.genre.edit(form, genres))
      }
    }
  }

  /**
   * 登録実行
   */
  def create = Action.async { implicit rs =>
    //implicitを書かないとエラーになる（bindFrmmRequest）
    genreForm.bindFromRequest.fold(
      error => {
        db.run(Genres.sortBy(t => t.id).result).map { genres =>
          BadRequest(views.html.genre.edit(error, genres))
        }
      },
      form => {
        val genre = GenresRow(0, form.name)
        //ユーザ情報の登録を行うDBIOActionを生成
        db.run(Genres += genre).map { _ =>
          Redirect(routes.GenreController.list)
        }
      }
    )
  }

  /**
   * 更新実行
   */
  def update = Action.async { implicit rs =>
    // リクエストの内容をバインド
    genreForm.bindFromRequest.fold(
      // エラーの場合は登録画面に戻す
      error => {
        db.run(Genres.sortBy(t => t.id).result).map { genres =>
          BadRequest(views.html.genre.edit(error, genres))
        }
      },
      // OKの場合は登録を行い一覧画面にリダイレクトする
      form  => {
        // ユーザ情報を更新
        val genre = GenresRow(form.id.get, form.name)
        db.run(Genres.filter(t => t.id === genre.id.bind).update(genre)).map { _ =>
          // 一覧画面にリダイレクト
          Redirect(routes.GenreController.list)
        }
      }
    )
  }

  /**
   * 削除実行
   */
  // def remove(id: Int) = TODO
  def remove(id: Int) = Action.async { implicit rs =>
    // ユーザを削除
    db.run(Genres.filter(t => t.id === id.bind).delete).map { _ =>
      // 一覧画面へリダイレクト
      Redirect(routes.GenreController.list)
    }
  }

}
