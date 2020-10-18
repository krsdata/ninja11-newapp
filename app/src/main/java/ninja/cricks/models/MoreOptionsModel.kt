package ninja.cricks.models

import ninja.cricks.R
import java.io.Serializable


class MoreOptionsModel :Serializable,Cloneable {

    var id: Int = 0
    var drawable: Int = R.drawable.logo_google
    var title: String = ""

}
