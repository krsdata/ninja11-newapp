package ninja.cricks.models

import ninja.cricks.R
import java.io.Serializable


data class MoreOptionsModel(
    var id: Int = 0,
    var drawable: Int = 0,
    var title: String = "",
    var imageUrl: String = ""
) : Serializable, Cloneable
