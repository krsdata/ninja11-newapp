package com.edify.atrist.listener

import fancode.cricks.models.MyTeamModels
import fancode.cricks.models.ContestModelLists

interface OnContestLoadedListener {
    fun onMyContest(contestModel: ArrayList<ContestModelLists>)
    fun onMyTeam(count: ArrayList<MyTeamModels>)
}