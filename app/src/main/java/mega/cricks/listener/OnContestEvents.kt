package com.edify.atrist.listener

import mega.cricks.ui.contest.models.ContestModelLists

interface OnContestEvents {
    fun onContestJoinning(objects:ContestModelLists,position: Int)
    fun onShareContest(objects:ContestModelLists)
}