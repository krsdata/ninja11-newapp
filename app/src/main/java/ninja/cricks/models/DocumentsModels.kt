package ninja.cricks.models

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable


data class DocumentsModels(
    var status: Int = 0,
    var message: String = "",

    @SerializedName("data")
    @Expose
    var documentDataModel: DocumentsDataModels? = null,
) : Serializable, Cloneable