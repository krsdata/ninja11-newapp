package com.edify.atrist.listener

import ninja.cricks.models.MyTeamModels
import ninja.cricks.ui.contest.models.ContestModelLists

interface OnContestLoadedListener {
    fun onMyContest(contestModel: ArrayList<ContestModelLists>)
    fun onMyTeam(count: ArrayList<MyTeamModels>)
}