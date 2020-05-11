package com.tencent.tic

interface TEduListener {
    fun onError(message: String)

    fun joinRoomSuccess()

    fun onUserJoin(userId: String)
    fun onUserLeave(userId: String)

    fun onUserJoinAudio(userId: String)
    fun onUserLeaveAudio(userId: String)

    fun onRoomClose()

    fun receiveToOneMessage(fromUserId: String, message: String)
    fun receiveToGroupMessage(fromUserId: String, message: String)
    fun onInfo(info: String)
}