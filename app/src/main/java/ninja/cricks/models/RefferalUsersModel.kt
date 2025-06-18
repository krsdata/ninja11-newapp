package ninja.cricks.models

import java.io.Serializable


data class RefferalUsersModel(

    var id: String = "",
    var name: String = "",
    var created_at: String = "",
    var referral_amount: String = ""
) : Serializable, Cloneable