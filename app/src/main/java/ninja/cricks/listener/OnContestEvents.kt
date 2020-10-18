package com.edify.atrist.listener

import ninja.cricks.ui.contest.models.ContestModelLists

interface OnContestEvents {
    fun onContestJoinning(objects:ContestModelLists,position: Int)
    fun onShareContest(objects:ContestModelLists)
}