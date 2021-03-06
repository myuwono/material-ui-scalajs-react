package wav.web.muiwrapper.sampler

import japgolly.scalajs.react._
import vdom.prefix_<^._
import extra._
import router2._
import wav.web.muiwrapper.mui._
import scala.scalajs.js, js.UndefOr._, js.Dynamic.{global => g}
import scalaz.Equal
import org.scalajs.dom
import Syntax._

sealed abstract class Example(val title: String,
                              val routerPath: String,
                              val render: () => ReactElement)

object Example {

  case object GettingStarted extends Example("Getting started", "GettingStarted", () => examples.GettingStarted.content())

  case object IconsAndInputs extends Example("Icons and Inputs", "IconsAndInputs", () => examples.IconsAndInputs.content())

  case object Dialogs extends Example("Dialogs", "Dialogs", () => examples.Dialogs.content())

  case object ListsAndMenus extends Example("Lists and Menus", "ListsAndMenus", () => examples.ListsAndMenus.content())

  implicit val equality   : Equal[Example]       = Equal.equalA
  implicit val reusability: Reusability[Example] = Reusability.byEqual

  val values = Vector[Example](GettingStarted, IconsAndInputs, Dialogs, ListsAndMenus)

  import ExamplesPage._

  def routes = RouterConfigDsl[Example].buildRule { dsl =>
    import dsl._
    values.map(eg =>
      staticRoute(eg.routerPath, eg) ~> renderR(ctl => components(Props(ctl, eg)))
    ).reduce(_ | _)
  }

}

object ExamplesPage {

  private def muiSpacing(get: js.Dynamic => js.Dynamic): String =
    get(muiUniverse.mui.Styles.Spacing).toString

  case class Props(router: RouterCtl[Example], current: Example)

  implicit val propsReuse = Reusability.caseclass2(Props.unapply)

  private val names = Example.values.map(_.title)

  // This is: https://github.com/callemall/material-ui/blob/master/docs/src/app/components/pages/page-with-nav.jsx
  val components = dynamicB[Props]("Components") { p =>
    val items: js.Array[Menu.Item] = Example.values.zipWithIndex.map { e =>
      val (eg, i) = e
      (MenuItem(
        key = i.toString,
        text = eg.title,
        payload = p.router.urlFor(eg).value): Menu.Item)
    }.toJsArray
    AppCanvas()(
      AppBar(
        title = "Components",
        iconElementLeft = <.div().render, // hide.
        iconElementRight = ?(IconButton(
          className = "fa",
          iconLigature = "fa-github",
          href = "https://github.com/wav/material-ui-scalajs-react",
          linkButton = true))),
      <.div(^.paddingTop := (muiSpacing(_.desktopKeylineIncrement) + "px"),
        Menu(
          selectedIndex = names.indexOf(p.current.title),
          onItemTap = ?((_, _, item) => dom.location.href = item.payload),
          style = js.Dictionary[String]("float" -> "left"),
          text = "",
          menuItems = items),
        <.div(
          ^.float.left,
          ^.padding := muiSpacing(_.desktopGutter) + "px",
          p.current.render())))
  }
    .configureSpec(installMuiContext)
    .configure(Reusability.shouldComponentUpdate)
    .propsRequired
    .build

}