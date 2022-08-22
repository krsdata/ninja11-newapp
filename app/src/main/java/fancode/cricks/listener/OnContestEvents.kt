package com.edify.atrist.listener

import fancode.cricks.models.ContestModelLists

interface OnContestEvents {
    fun onContestJoinning(objects: ContestModelLists, position: Int)
    fun onShareContest(objects: ContestModelLists)
}