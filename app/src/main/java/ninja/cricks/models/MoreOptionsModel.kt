package ninja.cricks.models

import ninja.cricks.R
import java.io.Serializable


data class MoreOptionsModel(

    var viewType: Int = 0,
    var id: Int = 0,
    var drawable: Int = 0,
    var title: String = "",
    var titleUrl: String = "",
    var imageUrl: String = ""
) : Serializable, Cloneable
