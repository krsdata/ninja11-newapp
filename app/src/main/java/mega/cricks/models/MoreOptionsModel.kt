package mega.cricks.models

import mega.cricks.R
import java.io.Serializable


class MoreOptionsModel :Serializable,Cloneable {

    var id: Int = 0
    var drawable: Int = R.drawable.logo_google
    var title: String = ""

}
