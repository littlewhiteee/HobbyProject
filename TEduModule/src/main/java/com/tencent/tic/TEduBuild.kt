package com.tencent.tic

import android.content.Context
import android.os.Handler
import android.view.View
import com.tencent.imsdk.TIMMessage
import com.tencent.tic.core.TICClassroomOption
import com.tencent.tic.core.TICManager
import com.tencent.tic.demo.activities.TICVideoRootView
import com.tencent.trtc.TRTCCloud
import com.tencent.trtc.TRTCCloudDef

class TEduBuild(
        val listener: TEduListener,
        private val isTeacher: Boolean
) {
    private var mRoomId: Int? = null
    private var mUserSig: String? = null
    private var mUserId: String? = null
    private lateinit var ticVideoRootView: TICVideoRootView
    private val ticManager: TICManager
    private val mHandler = Handler()
    private var isPushVideo = false
    private var isPushAudio = false

    var mTrtcCloud: TRTCCloud? = null

    init {
        ticManager = mTicManager!!
    }

    fun isVideoOpen() = isPushVideo
    fun isAudioOpen() = isPushAudio

    fun createRoomAndJoin(rootView: TICVideoRootView, userId: String, userSig: String, roomId: Int) {
        mUserId = userId
        mUserSig = userSig
        mRoomId = roomId
        ticVideoRootView = rootView
        listener.onInfo("createRoomAndJoin $userId $roomId")
        ticManager.login(userId, userSig, object : TICManager.TICCallback<Any> {
            override fun onSuccess(data: Any?) {
                listener.onInfo("login onSuccess")
                mHandler.post {
                    if (isTeacher) {
                        createClassRoom { result ->
                            if (result) {
                                initTrtc()
                                joinClassRoom()
                                ticManager.addIMMessageListener(messageListener)
                                ticManager.addEventListener(eventListener)
                            }
                        }
                    } else {
                        initTrtc()
                        joinClassRoom()
                        ticManager.addIMMessageListener(messageListener)
                        ticManager.addEventListener(eventListener)
                    }
                }
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                listener.onInfo("login onError")
                mHandler.post { listener.onError("$userId:登录失败, err:$errCode  msg: $errMsg") }
            }
        })
    }

    private fun initTrtc() {
        //1、获取trtc
        mTrtcCloud = ticManager.trtcClound
        mTrtcCloud?.setGSensorMode(TRTCCloudDef.TRTC_GSENSOR_MODE_UIAUTOLAYOUT)
        //2、TRTC View
        ticVideoRootView.setUserId(mUserId)
        val localVideoView = ticVideoRootView.getCloudVideoViewByIndex(0)
        localVideoView.userId = mUserId

        //3、开始本地视频图像
        startLocalVideo(isTeacher)
        //4. 开始音频
        enableAudio(isTeacher)
    }

    private fun startLocalVideo(enable: Boolean) {
        val usrid = mUserId
        val localVideoView = ticVideoRootView.getCloudVideoViewByUseId(usrid)
        localVideoView?.userId = usrid
        localVideoView?.visibility = View.VISIBLE
        if (enable) {
            mTrtcCloud?.startLocalPreview(false, localVideoView)
            mTrtcCloud?.setRemoteViewFillMode(usrid, TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT)
        } else {
            mTrtcCloud?.stopLocalPreview()
        }
        isPushVideo = enable
    }

    fun enableAudio(enable: Boolean) {
        if (enable) {
            mTrtcCloud?.startLocalAudio()
        } else {
            mTrtcCloud?.stopLocalAudio()
        }
        isPushAudio = enable
    }

    fun enableVideo(enable: Boolean) {
        startLocalVideo(enable)
    }

    fun switchCamera() {
        if (isPushVideo) {
            mTrtcCloud?.switchCamera()
        }
    }

    /**
     * 进入课堂
     */
    private fun createClassRoom(resultCall: ((success: Boolean) -> Unit)) {
        ticManager.createClassroom(
                mRoomId!!,
                TICManager.TICClassScene.TIC_CLASS_SCENE_LIVE,
                object : TICManager.TICCallback<Any> {
                    override fun onSuccess(data: Any) {
                        listener.onInfo("createClassroom onSuccess")
                        mHandler.post {
                            resultCall.invoke(true)
                        }
                    }

                    override fun onError(module: String, errCode: Int, errMsg: String) {
                        listener.onInfo("createClassroom onError")
                        mHandler.post {
                            when (errCode) {
                                10021 -> {
                                    resultCall.invoke(true)
                                    listener.onInfo("该课堂已被他人创建，请\"加入课堂\"")
                                }
                                10025 -> {
                                    resultCall.invoke(true)
                                    listener.onInfo("该课堂已创建，请\"加入课堂\"")
                                }
                                else -> {
                                    resultCall.invoke(false)
                                    listener.onError("创建课堂失败, 房间号：${mRoomId} err:$errCode msg:$errMsg")
                                }
                            }
                        }
                    }
                })
    }

    private fun joinClassRoom() {
        val classroomOption = TICClassroomOption()
        classroomOption.classId = mRoomId!!
        ticManager.joinClassroom(classroomOption, object : TICManager.TICCallback<Any> {
            override fun onSuccess(data: Any?) {
                listener.onInfo("joinClassroom onSuccess")
                mHandler.post { listener.joinRoomSuccess() }
            }

            override fun onError(module: String?, errCode: Int, errMsg: String?) {
                listener.onInfo("joinClassroom onError")
                listener.onError("创建课堂失败, 房间号：${mRoomId} err:$errCode msg:$errMsg")
            }
        })
    }

    private val messageListener = object : TICManager.TICMessageListener {
        override fun onTICRecvTextMessage(fromUserId: String?, text: String?) {
            listener.onInfo("onTICRecvTextMessage")
            mHandler.post {
                listener.receiveToOneMessage(fromUserId ?: return@post, text ?: return@post)
            }
        }

        override fun onTICRecvMessage(message: TIMMessage?) {
            listener.onInfo("onTICRecvMessage")
        }

        override fun onTICRecvGroupTextMessage(fromUserId: String?, text: String?) {
            listener.onInfo("onTICRecvGroupTextMessage")
            mHandler.post {
                listener.receiveToGroupMessage(fromUserId ?: return@post, text ?: return@post)
            }
        }

        override fun onTICRecvCustomMessage(fromUserId: String?, data: ByteArray?) {
            val text = data?.let { String(it) } ?: return
            onTICRecvTextMessage(fromUserId, text)
        }

        override fun onTICRecvGroupCustomMessage(fromUserId: String?, data: ByteArray?) {
            val text = data?.let { String(it) } ?: return
            onTICRecvGroupTextMessage(fromUserId, text)
        }
    }
    private val eventListener = object : TICManager.TICEventListener {
        override fun onTICUserVideoAvailable(userId: String?, available: Boolean) {
            listener.onInfo("onTICUserVideoAvailable $userId $available")
            if (available) {
                val renderView =
                        ticVideoRootView.onMemberEnter(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG)
                if (renderView != null) {
                    // 启动远程画面的解码和显示逻辑，FillMode 可以设置是否显示黑边
                    mTrtcCloud?.setRemoteViewFillMode(
                            userId,
                            TRTCCloudDef.TRTC_VIDEO_RENDER_MODE_FIT
                    )
                    mTrtcCloud?.startRemoteView(userId, renderView)
                    renderView.userId = userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG
                }
            } else {
                mTrtcCloud?.stopRemoteView(userId)
                ticVideoRootView.onMemberLeave(userId + TRTCCloudDef.TRTC_VIDEO_STREAM_TYPE_BIG)
            }
        }

        override fun onTICMemberQuit(userList: MutableList<String>?) {
            listener.onInfo("onTICMemberQuit ${userList?.toTypedArray()}")
            mHandler.post {
                userList?.forEach {
                    listener.onUserLeave(it)
                }
            }
        }

        override fun onTICClassroomDestroy() {
            listener.onInfo("onTICClassroomDestroy")
            mHandler.post { listener.onRoomClose() }
        }

        override fun onTICUserSubStreamAvailable(userId: String?, available: Boolean) {
            listener.onInfo("onTICUserSubStreamAvailable $userId $available")
        }

        override fun onTICUserAudioAvailable(userId: String?, available: Boolean) {
            listener.onInfo("onTICUserAudioAvailable $userId $available")
            mHandler.post {
                if (available) {
                    listener.onUserJoinAudio(userId!!)
                } else {
                    listener.onUserLeaveAudio(userId!!)
                }
            }
        }

        override fun onTICSendOfflineRecordInfo(code: Int, desc: String?) {
            listener.onInfo("onTICSendOfflineRecordInfo $code $desc")
        }

        override fun onTICMemberJoin(userList: MutableList<String>?) {
            listener.onInfo("onTICMemberJoin ${userList?.toTypedArray()}")
            mHandler.post {
                userList?.forEach {
                    listener.onUserJoin(it)
                }
            }
        }

        override fun onTICVideoDisconnect(errCode: Int, errMsg: String?) {
            listener.onInfo("onTICVideoDisconnect $errCode $errMsg")
        }
    }

    fun sendGroupMessage(message: String, resultCall: ((success: Boolean) -> Unit)? = null) {
        listener.onInfo("sendGroupMessage $message ")
        ticManager.sendGroupCustomMessage(
                message.toByteArray(),
                object : TICManager.TICCallback<Any> {
                    override fun onSuccess(data: Any?) {
                        mHandler.post { resultCall?.invoke(true) }
                    }

                    override fun onError(module: String?, errCode: Int, errMsg: String?) {
                        mHandler.post { resultCall?.invoke(false) }
                    }
                })
    }

    fun sendToOneMessage(targetID: String, message: String, resultCall: ((success: Boolean) -> Unit)? = null) {
        listener.onInfo("sendToOneMessage $message ")
        ticManager.sendCustomMessage(targetID, message.toByteArray(),
                object : TICManager.TICCallback<TIMMessage> {
                    override fun onSuccess(data: TIMMessage?) {
                        mHandler.post { resultCall?.invoke(true) }
                    }

                    override fun onError(module: String?, errCode: Int, errMsg: String?) {
                        mHandler.post { resultCall?.invoke(false) }
                    }
                })
    }

    fun setLocalViewRotation(rotation: Int) {
        mTrtcCloud?.setLocalViewRotation(rotation)
    }

    fun release(resultCall: ((success: Boolean) -> Unit)? = null) {
        mTrtcCloud?.exitRoom()
        mTrtcCloud?.stopLocalPreview()
        enableAudio(false)

        ticManager.removeIMMessageListener(messageListener)
        ticManager.removeEventListener(eventListener)
        if (isTeacher) {
            ticManager.destroyClassroom(mRoomId!!, null)
            mHandler.post { resultCall?.invoke(true) }
        } else {
            ticManager.quitClassroom(!isTeacher, object : TICManager.TICCallback<Any> {
                override fun onSuccess(data: Any?) {
                    mHandler.post { resultCall?.invoke(true) }
                }

                override fun onError(module: String?, errCode: Int, errMsg: String?) {
                    mHandler.post { resultCall?.invoke(false) }
                }
            })
        }
    }

    companion object {
        const val APPID = 1400229353
        private var mTicManager: TICManager? = null
        @JvmStatic
        fun init(context: Context) {
            if (mTicManager == null) {
                mTicManager = TICManager.getInstance()
                mTicManager?.init(context, APPID)
            }
        }
    }
}