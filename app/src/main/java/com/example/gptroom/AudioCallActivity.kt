package com.example.gptroom

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.View.OnClickListener
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.gptroom.utils.DeviceUtils
import com.netease.lava.nertc.sdk.NERtcCallback
import com.netease.lava.nertc.sdk.NERtcConstants
import com.netease.lava.nertc.sdk.NERtcEx
import com.netease.lava.nertc.sdk.NERtcOption
import com.netease.lava.nertc.sdk.NERtcParameters
import com.netease.lava.nertc.sdk.NERtcUserJoinExtraInfo
import com.netease.lava.nertc.sdk.NERtcUserLeaveExtraInfo
import com.netease.lava.nertc.sdk.stats.NERtcAudioRecvStats
import com.netease.lava.nertc.sdk.stats.NERtcAudioSendStats
import com.netease.lava.nertc.sdk.stats.NERtcNetworkQualityInfo
import com.netease.lava.nertc.sdk.stats.NERtcStats
import com.netease.lava.nertc.sdk.stats.NERtcStatsObserver
import com.netease.lava.nertc.sdk.stats.NERtcVideoRecvStats
import com.netease.lava.nertc.sdk.stats.NERtcVideoSendStats
import com.netease.lite.BuildConfig


class AudioCallActivity : AppCompatActivity() ,NERtcCallback,OnClickListener{
    private val TAG = "AudioCallActivity"
    private val APP_KEY = "3c4f31f7f277ac27ec689b97b304da6d"

    private var mRoomId: String? = "GPT40666888"
    private var mUserId: Long = 0
    private var mEnableLocalAudio = true
    private var mJoinChannel = false
    private var mIsSpeakerPhone = true

    private var mContainer: RelativeLayout? = null
    private var mRemoteUserVv: ArrayList<ImageView>? = null
    private var mRemoteUserId: ArrayList<TextView>? = null
    private var mRemoteVolume: ArrayList<TextView>? = null
    private var mRemoteNetwork: ArrayList<TextView>? = null

    private var mAudioRouteBtn: Button? = null
    private var mMuteMicBtn: Button? = null
    private var mHangUpBtn: Button? = null
    private var mBackIv: ImageView? = null
    private var mRoomTittleTv: TextView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_call)
        mUserId = DeviceUtils.getUUID(this);
        initView()
        setupNERtc()
        joinChannel(mUserId, mRoomId)
    }



    private fun initView() {
        mContainer = findViewById(R.id.rl_main_container)
        mRemoteUserVv = java.util.ArrayList()
        mRemoteUserVv!!.add((findViewById<View>(R.id.user_img1) as ImageView))

        mRemoteUserId = java.util.ArrayList()
        mRemoteUserId!!.add((findViewById<View>(R.id.user_id1) as TextView))
        mRemoteVolume = java.util.ArrayList()
        mRemoteVolume!!.add((findViewById<View>(R.id.user_voice1) as TextView))

        mRemoteNetwork = java.util.ArrayList()


        mRemoteNetwork!!.add((findViewById<View>(R.id.user_net1) as TextView))
        mAudioRouteBtn = findViewById(R.id.btn_audio_route)
        mMuteMicBtn = findViewById(R.id.btn_mute_audio)
        mHangUpBtn = findViewById(R.id.btn_hangup)
        mBackIv = findViewById(R.id.iv_back)
        mRoomTittleTv = findViewById(R.id.tv_room_id)
        mBackIv?.setOnClickListener(this)
        mAudioRouteBtn?.setOnClickListener(this)
        mMuteMicBtn?.setOnClickListener(this)
        mHangUpBtn?.setOnClickListener(this)
        if (!TextUtils.isEmpty(mRoomId)) {
            mRoomTittleTv?.text = "房间号:$mRoomId"
        }
    }

    /**
     * 初始化NERtc
     */
    private fun setupNERtc() {
        val parameters = NERtcParameters()
        NERtcEx.getInstance().setParameters(parameters) //先设置参数，后初始化
        val options = NERtcOption()
        if (BuildConfig.DEBUG) {
            options.logLevel = NERtcConstants.LogLevel.INFO
        } else {
            options.logLevel = NERtcConstants.LogLevel.WARNING
        }
        try {
            NERtcEx.getInstance().init(applicationContext, APP_KEY, this, options)
        } catch (e: Exception) {
            // 可能由于没有release导致初始化失败，release后再试一次
            NERtcEx.getInstance().release()
            try {
                NERtcEx.getInstance().init(applicationContext, APP_KEY, this, options)
            } catch (ex: Exception) {
                Toast.makeText(this, "SDK初始化失败", Toast.LENGTH_LONG).show()
                finish()
                return
            }
        }
        //设置质量透明回调
        NERtcEx.getInstance().setStatsObserver(object : NERtcStatsObserver {
            override fun onRtcStats(neRtcStats: NERtcStats) {}
            override fun onLocalAudioStats(neRtcAudioSendStats: NERtcAudioSendStats) {}
            override fun onRemoteAudioStats(neRtcAudioRecvStats: Array<NERtcAudioRecvStats>) {
                Log.d(TAG, "onRemoteAudioStats:" + neRtcAudioRecvStats.size)
                var index = 0
                while (index < neRtcAudioRecvStats.size) {
                    if (index < mRemoteVolume!!.size) {
                        val tmp = neRtcAudioRecvStats[index].layers[0]
                        mRemoteVolume!![index].text =
                            "uId：" + neRtcAudioRecvStats[index].uid + "  音量：" + tmp.volume
                        mRemoteVolume!![index].visibility = View.VISIBLE
                    }
                    index++
                }
                while (index < mRemoteVolume!!.size) {
                    mRemoteVolume!![index].visibility = View.GONE
                    index++
                }
            }

            override fun onLocalVideoStats(neRtcVideoSendStats: NERtcVideoSendStats) {}
            override fun onRemoteVideoStats(neRtcVideoRecvStats: Array<NERtcVideoRecvStats>) {}
            override fun onNetworkQuality(neRtcNetworkQualityInfos: Array<NERtcNetworkQualityInfo>) {
                Log.d(TAG, "onNetworkQuality:" + neRtcNetworkQualityInfos.size)
                var index = 0
                while (index < neRtcNetworkQualityInfos.size) {
                    if (index < mRemoteNetwork!!.size) {
                        val tmp = neRtcNetworkQualityInfos[index]
                        if (tmp.userId == mUserId) {
                            index++
                            continue
                        }
                        mRemoteNetwork!![index].text =
                            "uId：" + tmp.userId + "  网络质量：" + NetQuality.getMsg(tmp.downStatus)
                        mRemoteNetwork!![index].visibility = View.VISIBLE
                    }
                    index++
                }
                while (index < mRemoteNetwork!!.size) {
                    mRemoteNetwork!![index].visibility = View.GONE
                    index++
                }
            }
        })
        setLocalAudioEnable(true)
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        super.onBackPressed()
        exit()
    }

    /**
     * 退出房间并关闭页面
     */
    private fun exit() {
        if (mJoinChannel) {
            leaveChannel()
        } else {
            finish() //其他Activity 都同样处理下
        }
    }

    /**
     * 离开房间
     *
     * @return
     */
    private fun leaveChannel(): Boolean {
        mJoinChannel = false
        setLocalAudioEnable(false)
        val ret: Int = NERtcEx.getInstance().leaveChannel()
        return ret == NERtcConstants.ErrorCode.OK
    }

    /**
     * 设置本地音频可用性
     *
     * @param enable
     */
    private fun setLocalAudioEnable(enable: Boolean) {
        mEnableLocalAudio = enable
        NERtcEx.getInstance().enableLocalAudio(mEnableLocalAudio)
    }

    /**
     * 加入房间
     *
     * @param userId 用户ID
     * @param roomId 房间ID
     */
    private fun joinChannel(userId: Long, roomId:  String ?) {
        Log.i(TAG, "joinChannel userId: $userId")
        NERtcEx.getInstance().joinChannel(null, roomId, userId)
    }

    override fun onJoinChannel(result: Int, channelId: Long, elapsed: Long, l2: Long) {
        Log.i(TAG, "onJoinChannel result: $result channelId: $channelId elapsed: $elapsed")
        if (result == NERtcConstants.ErrorCode.OK) {
            mJoinChannel = true
        }
    }
    override fun onLeaveChannel(result: Int) {
        Log.i(TAG, "onLeaveChannel result: $result")
        finish()
    }
    override fun onUserJoined(userId: Long) {
        Log.i(TAG, "onUserJoined userId: $userId")
        for (i in mRemoteUserVv!!.indices) {
            if (mRemoteUserVv!![i].tag == null && mRemoteUserId!![i].tag == null) {
                mRemoteUserVv!![i].setImageResource(R.drawable.gpt)
                mRemoteUserVv!![i].tag = userId
                mRemoteUserId!![i].text = "uId:$userId"
                mRemoteUserId!![i].tag = userId
                mRemoteUserId!![i].visibility = View.VISIBLE
                break
            }
        }
    }

    override fun onUserJoined(uid: Long, joinExtraInfo: NERtcUserJoinExtraInfo?) {
        Log.i(TAG, "onUserJoined uid: $uid")

    }

    override fun onUserLeave(userId: Long, i: Int) {
        Log.i(TAG, "onUserLeave uid: $userId")
        val userView = mContainer!!.findViewWithTag<ImageView>(userId)
        if (userView != null) {
            //设置TAG为null，代表当前没有订阅
            userView.tag = null
            userView.setImageResource(R.mipmap.common_user_portrait)
        }
        val userIdView = mContainer!!.findViewWithTag<TextView>(userId)
        if (userIdView != null) {
            //设置TAG为null，代表当前没有订阅
            userIdView.tag = null
            userIdView.visibility = View.GONE
        }
    }


    override fun onUserLeave(uid: Long, reason: Int, leaveExtraInfo: NERtcUserLeaveExtraInfo?) {}

    override fun onUserAudioStart(userId: Long) {
        Log.i(TAG, "onUserAudioStart uid: $userId")
    }

    override fun onUserAudioStop(userId: Long) {
        Log.i(TAG, "onUserAudioStop uid: $userId")
    }

    override fun onUserVideoStart(userId: Long, profile: Int) {
        Log.i(TAG, "onUserVideoStart uid: $userId profile: $profile")
    }

    override fun onUserVideoStop(userId: Long) {
        Log.i(TAG, "onUserVideoStop, uid=$userId")
    }

    override fun onDisconnect(i: Int) {
        Log.i(TAG, "onDisconnect uid: $i")
        finish()
    }

    override fun onClientRoleChange(old: Int, newRole: Int) {
        Log.i(TAG, "onUserAudioStart old: $old, newRole : $newRole")
    }

    /**
     * 切换听筒和扬声器
     */
    private fun changeAudioRoute() {
        mIsSpeakerPhone = !mIsSpeakerPhone
        NERtcEx.getInstance().setSpeakerphoneOn(mIsSpeakerPhone)
        if (mIsSpeakerPhone) {
            mAudioRouteBtn!!.text = "使用听筒"
        } else {
            mAudioRouteBtn!!.text = "使用扬声器"
        }
    }

    /**
     * 改变音频可用状态
     */
    private fun changeAudioEnable() {
        mEnableLocalAudio = !mEnableLocalAudio
        setLocalAudioEnable(mEnableLocalAudio)
        if (mEnableLocalAudio) {
            mMuteMicBtn!!.text = "关闭麦克风"
        } else {
            mMuteMicBtn!!.text = "打开麦克风"
        }
    }

    override fun onClick(view: View) {
        val id = view.id
        if (id == R.id.iv_back) {
            exit()
        } else if (id == R.id.btn_audio_route) {
            changeAudioRoute()
        } else if (id == R.id.btn_mute_audio) {
            changeAudioEnable()
        } else if (id == R.id.btn_hangup) {
            hangup()
        }
    }

    /**
     * 挂断
     */
    private fun hangup() {
        exit()
    }

    /**
     * 网络状态枚举
     */
    enum class NetQuality(private val num: Int, private val msg: String) {
        UNKNOWN(0, "未知"),
        EXCELLENT(1, "非常好"),
        GOOD(2, "好"),
        POOR(3, "不太好"),
        BAD(4, "差"),
        VERYBAD(5, "非常差"),
        DOWN(6, "无网络");

        companion object {
            fun getMsg(code: Int): String {
                for (item in entries) {
                    if (item.num == code) {
                        return item.msg
                    }
                }
                return "未定义"
            }
        }
    }

}