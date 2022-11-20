package ninja.cricks.adaptors

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt
import ninja.cricks.SelectTeamActivity
import ninja.cricks.models.MyTeamModels
import ninja.cricks.R
=======
import ninja.cricks.R
import ninja.cricks.SelectTeamActivity
import ninja.cricks.models.MyTeamModels
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt
import ninja.cricks.models.SelectedTeamModels
import ninja.cricks.models.UpcomingMatchesModel
import ninja.cricks.utils.CustomeProgressDialog


class SelectedTeamAdapter(
    val context: SelectTeamActivity,
    val matchObject: UpcomingMatchesModel,
    val customeProgressDialog: CustomeProgressDialog,
    val tradeinfoModels: ArrayList<SelectedTeamModels>
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var onItemClick: ((SelectedTeamModels) -> Unit)? = null
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt
    var mContext:SelectTeamActivity ? =context
    private var matchesListObject =  tradeinfoModels
=======
    var mContext: SelectTeamActivity? = context
    private var matchesListObject = tradeinfoModels
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt

    companion object {
        const val TYPE_CLOSED = 1
        const val TYPE_OPENED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val comparable = matchesListObject.get(position)
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt
         if(comparable.closeTeamList!=null &&comparable.closeTeamList!!.size>0) {
             return TYPE_CLOSED
         }else {
             return  TYPE_OPENED
         }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType== TYPE_CLOSED){
            var view = LayoutInflater.from(parent.context)
                .inflate(R.layout.selected_team_created_label, parent, false)
            return ViewHolderJoinedMatches(view)
        }else if(viewType== TYPE_OPENED){
            var view = LayoutInflater.from(parent.context)
=======
        if (comparable.closeTeamList != null && comparable.closeTeamList!!.size > 0) {
            return TYPE_CLOSED
        } else {
            return TYPE_OPENED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == TYPE_CLOSED) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.selected_team_created_label, parent, false)
            return ViewHolderJoinedMatches(view)
        } else if (viewType == TYPE_OPENED) {
            val view = LayoutInflater.from(parent.context)
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt
                .inflate(R.layout.unselectedselected_team_created_labe, parent, false)
            return UpcomingMatchesViewHolder(view)
        }
        return null!!
    }

    override fun onBindViewHolder(parent: RecyclerView.ViewHolder, viewType: Int) {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt


        if(parent.itemViewType==TYPE_CLOSED){
            var objectVal = matchesListObject[viewType]
            val viewJoinedMatches: ViewHolderJoinedMatches = parent as ViewHolderJoinedMatches
            viewJoinedMatches.recyclerView.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
            var adapter = ClosedTeamsAdapter(
=======
        if (parent.itemViewType == TYPE_CLOSED) {
            val objectVal = matchesListObject[viewType]
            val viewJoinedMatches: ViewHolderJoinedMatches = parent as ViewHolderJoinedMatches
            viewJoinedMatches.recyclerView.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
            val adapter = ClosedTeamsAdapter(
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt
                mContext!!,
                matchObject,
                customeProgressDialog,
                objectVal.closeTeamList!!
            )
            viewJoinedMatches.recyclerView.adapter = adapter
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt
        } else  if(parent.itemViewType== TYPE_OPENED) {
            var objectVal = matchesListObject[viewType]
=======
        } else if (parent.itemViewType == TYPE_OPENED) {
            val objectVal = matchesListObject[viewType]
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt
            val viewholderOpenTeam: UpcomingMatchesViewHolder = parent as UpcomingMatchesViewHolder
            viewholderOpenTeam.recyclerView.layoutManager =
                LinearLayoutManager(mContext, RecyclerView.VERTICAL, false)
            if (objectVal.openTeamList != null && objectVal.openTeamList!!.size > 0) {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt
                var adapter = OpenTeamsAdapter(
=======
                val adapter = OpenTeamsAdapter(
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt
                    mContext!!,
                    matchObject,
                    customeProgressDialog,
                    objectVal.openTeamList!!
                )
                adapter.setOnCheckChangedListeners(View.OnClickListener {
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt
                   checkforAllSelections(objectVal.openTeamList, viewholderOpenTeam.checkAll)

                })
                viewholderOpenTeam.checkAll.setOnClickListener(View.OnClickListener {
                        if(viewholderOpenTeam.checkAll.isChecked){
                              for (x in 0..objectVal.openTeamList!!.size-1){
                                  var values = objectVal.openTeamList!!.get(x)
                                  values.isSelected = true
                                  objectVal.openTeamList!!.set(x,values)

                              }
                        }else {
                            for (x in 0..objectVal.openTeamList!!.size-1){
                                var values = objectVal.openTeamList!!.get(x)
                                values.isSelected = false
                                objectVal.openTeamList!!.set(x,values)
                            }
                        }
=======
                    checkforAllSelections(objectVal.openTeamList, viewholderOpenTeam.checkAll)

                })
                viewholderOpenTeam.checkAll.setOnClickListener(View.OnClickListener {
                    if (viewholderOpenTeam.checkAll.isChecked) {
                        for (x in 0 until objectVal.openTeamList!!.size) {
                            val values = objectVal.openTeamList!!.get(x)
                            values.isSelected = true
                            objectVal.openTeamList!!.set(x, values)

                        }
                    } else {
                        for (x in 0 until objectVal.openTeamList!!.size) {
                            val values = objectVal.openTeamList!!.get(x)
                            values.isSelected = false
                            objectVal.openTeamList!!.set(x, values)
                        }
                    }
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt

                    adapter.notifyDataSetChanged()
                })


                viewholderOpenTeam.recyclerView.adapter = adapter
                adapter.onItemClick = { objects ->

                    objects.isSelected = !objects.isSelected!!
                    adapter.notifyDataSetChanged()

                    checkforAllSelections(objectVal.openTeamList, viewholderOpenTeam.checkAll)
                }

            }
        }
    }

    private fun checkforAllSelections(
        openlist: ArrayList<MyTeamModels>?,
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt
        checkAll: CheckBox) {
        var isAllChecked = false
        for (x in 0..openlist!!.size-1){
            var values = openlist.get(x)
            if(values.isSelected!!){
                isAllChecked = true
            }else {
=======
        checkAll: CheckBox
    ) {
        var isAllChecked = false
        for (x in 0 until openlist!!.size) {
            val values = openlist.get(x)
            if (values.isSelected!!) {
                isAllChecked = true
            } else {
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt
                isAllChecked = false
                break
            }

        }
<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt

=======
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt
        checkAll.isChecked = isAllChecked
    }

    override fun getItemCount(): Int {
        return matchesListObject.size
    }

<<<<<<< Updated upstream:app/src/main/java/ninja/cricks/adaptors/SelectedTeamAdapter.kt
    inner  class ViewHolderJoinedMatches(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_joined_team)
    }

    inner  class UpcomingMatchesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView = itemView.findViewById<RecyclerView>(R.id.recycler_joined_team)
        val checkAll = itemView.findViewById<CheckBox>(R.id.checkbox_selected_team)

    }


=======
    inner class ViewHolderJoinedMatches(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_joined_team)
    }

    inner class UpcomingMatchesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val recyclerView: RecyclerView = itemView.findViewById(R.id.recycler_joined_team)
        val checkAll: CheckBox = itemView.findViewById(R.id.checkbox_selected_team)
    }
>>>>>>> Stashed changes:app/src/main/java/fancode/cricks/adaptors/SelectedTeamAdapter.kt
}

