package com.edify.atrist.listener

import mega.cricks.models.MyTeamModels
import mega.cricks.ui.contest.models.ContestModelLists

interface OnContestLoadedListener {
    fun onMyContest(contestModel: ArrayList<ContestModelLists>)
    fun onMyTeam(count: ArrayList<MyTeamModels>)
}