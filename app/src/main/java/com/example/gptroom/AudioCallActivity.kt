package com.example.gptroom

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView
import com.example.gptroom.utils.DeviceUtils
import com.netease.lava.nertc.sdk.NERtc
import com.netease.lava.nertc.sdk.NERtcCallback
import com.netease.lava.nertc.sdk.NERtcConstants
import com.netease.lava.nertc.sdk.NERtcEx
import com.netease.lava.nertc.sdk.NERtcOption
import com.netease.lava.nertc.sdk.NERtcParameters
import com.netease.lava.nertc.sdk.NERtcUserJoinExtraInfo
import com.netease.lava.nertc.sdk.NERtcUserLeaveExtraInfo
import com.netease.lava.nertc.sdk.audio.NERtcAudioFrame
import com.netease.lava.nertc.sdk.audio.NERtcAudioFrameObserver
import com.netease.lava.nertc.sdk.audio.NERtcAudioFrameOpMode
import com.netease.lava.nertc.sdk.audio.NERtcAudioFrameRequestFormat
import com.netease.lava.nertc.sdk.stats.NERtcAudioRecvStats
import com.netease.lava.nertc.sdk.stats.NERtcAudioSendStats
import com.netease.lava.nertc.sdk.stats.NERtcNetworkQualityInfo
import com.netease.lava.nertc.sdk.stats.NERtcStats
import com.netease.lava.nertc.sdk.stats.NERtcStatsObserver
import com.netease.lava.nertc.sdk.stats.NERtcVideoRecvStats
import com.netease.lava.nertc.sdk.stats.NERtcVideoSendStats
import com.netease.lite.BuildConfig
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class AudioCallActivity : AppCompatActivity(), NERtcCallback, View.OnClickListener {
    private val TAG = "AudioCallActivity"
    private val APP_KEY = "3c4f31f7f277ac27ec689b97b304da6d"

    private var gptId: Long = 666888123456789
    private var mRoomId: String? = "1382"
    private var mUserId: Long = 0
    private var mEnableLocalAudio = true
    private var mJoinChannel = false
    private var mIsSpeakerPhone = true

    private var mContainer: RelativeLayout? = null

    private var mAudioRouteBtn: Button? = null
    private var mMuteMicBtn: Button? = null
    private var mHangUpBtn: Button? = null
    private var mBackIv: ImageView? = null
    private var mRoomTittleTv: TextView? = null
    private var mStatus: TextView? = null
    private var mGptview: LottieAnimationView? = null
    private var mRecordTag: LottieAnimationView? = null

    private var mImageButtonClose: ImageButton? = null

    private var isListening = false
    private val sampleRate = 44100
    private val audioSource = MediaRecorder.AudioSource.MIC
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT
    private val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

    var gptCALL = false;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_audio_call)
        mUserId = DeviceUtils.getUUID(this);
        initView()
        setRecordAudioParameters()
        setPlaybackAudioParameters()
        setupNERtc()
        joinChannel(mUserId, mRoomId)

    }

    private fun setPlaybackAudioParameters() {
        val formatMix = NERtcAudioFrameRequestFormat()
        //单声道、双声道
        formatMix.channels = 1
        //采样率
        formatMix.sampleRate = 32000
        //读写权限
        formatMix.opMode = NERtcAudioFrameOpMode.kNERtcAudioFrameOpModeReadWrite
        NERtcEx.getInstance().setPlaybackAudioFrameParameters(formatMix)
    }

    private fun setRecordAudioParameters() {
        val formatMix = NERtcAudioFrameRequestFormat()
        //单声道、双声道
        formatMix.channels = 1
        //采样率
        formatMix.sampleRate = 32000
        //读写权限
        formatMix.opMode = NERtcAudioFrameOpMode.kNERtcAudioFrameOpModeReadWrite
        NERtcEx.getInstance().setRecordingAudioFrameParameters(formatMix)
    }
    @SuppressLint("ObjectAnimatorBinding")
    private fun initView() {
        mContainer = findViewById(R.id.rl_main_container)
        mGptview = findViewById(R.id.gptview)
        mStatus = findViewById(R.id.status)
        mRecordTag = findViewById(R.id.record_tag)
        mRoomTittleTv = findViewById(R.id.tv_room_id)
        mImageButtonClose = findViewById(R.id.image_close)
        mBackIv?.setOnClickListener(this)
        mAudioRouteBtn?.setOnClickListener(this)
        mMuteMicBtn?.setOnClickListener(this)
        mHangUpBtn?.setOnClickListener(this)
        mImageButtonClose?.setOnClickListener(this)

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
            override fun onRtcStats(neRtcStats: NERtcStats) {
                //  Log.d(TAG, "onRtcStats:" + neRtcStats.toString())

            }

            override fun onLocalAudioStats(neRtcAudioSendStats: NERtcAudioSendStats) {

                Log.d(TAG, "onLocalAudioStats:" + neRtcAudioSendStats.toString())
            }

            override fun onRemoteAudioStats(neRtcAudioRecvStats: Array<NERtcAudioRecvStats>) {
//                Log.d(TAG, "onRemoteAudioStats:" + neRtcAudioRecvStats.size)
//                val tmp = neRtcAudioRecvStats[0].layers[0]
//                Log.d(TAG, "音量:" + tmp.volume)

            }

            override fun onLocalVideoStats(neRtcVideoSendStats: NERtcVideoSendStats) {}
            override fun onRemoteVideoStats(neRtcVideoRecvStats: Array<NERtcVideoRecvStats>) {}
            override fun onNetworkQuality(neRtcNetworkQualityInfos: Array<NERtcNetworkQualityInfo>) {
//                Log.d(TAG, "onNetworkQuality:" + neRtcNetworkQualityInfos.size)
//                val tmp = neRtcNetworkQualityInfos[0]
//                Log.d(TAG, "网络质量:" + NetQuality.getMsg(tmp.downStatus) + "---")
            }
        })

//        NERtcEx.getInstance().setAudioFrameObserver(object : NERtcAudioFrameObserver {
//            override fun onRecordFrame(neRtcAudioFrame: NERtcAudioFrame) {
//                Log.d(
//                    TAG, "onRecordFrame:" + neRtcAudioFrame.data
//                )
//            }
//
//            override fun onRecordSubStreamAudioFrame(neRtcAudioFrame: NERtcAudioFrame) {
//                Log.d(
//                    TAG, "onRecordSubStreamAudioFrame:" + neRtcAudioFrame.data
//                )
//            }
//
//            override fun onPlaybackFrame(neRtcAudioFrame: NERtcAudioFrame) {
//                Log.d(
//                    TAG, "onPlaybackFrame"
//                )
//            }
//
//            override fun onPlaybackAudioFrameBeforeMixingWithUserID(
//                l: Long, neRtcAudioFrame: NERtcAudioFrame
//            ) {
//                Log.d(
//                    TAG, "onPlaybackAudioFrameBeforeMixingWithUserID"
//                )
//            }
//
//            override fun onPlaybackAudioFrameBeforeMixingWithUserID(
//                l: Long, neRtcAudioFrame: NERtcAudioFrame, l1: Long
//            ) {
//                Log.d(
//                    TAG, "onPlaybackAudioFrameBeforeMixingWithUserID"
//                )
//            }
//
//            override fun onMixedAudioFrame(neRtcAudioFrame: NERtcAudioFrame) {
//                Log.d(
//                    TAG, "onMixedAudioFrame"
//                )
//            }
//
//            override fun onPlaybackSubStreamAudioFrameBeforeMixingWithUserID(
//                l: Long, neRtcAudioFrame: NERtcAudioFrame, l1: Long
//            ) {
//                Log.d(
//                    TAG, "onPlaybackSubStreamAudioFrameBeforeMixingWithUserID"
//                )
//            }
//        })
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
        NERtc.getInstance().setAudioProfile(
            NERtcConstants.AudioScenario.SPEECH, NERtcConstants.AudioProfile.MIDDLE_QUALITY
        )

    }

    /**
     * 加入房间
     *
     * @param userId 用户ID
     * @param roomId 房间ID
     */
    private fun joinChannel(userId: Long, roomId: String?) {
        Log.i(TAG, "joinChannel userId: $userId")
        NERtcEx.getInstance().joinChannel(null, roomId, userId)
    }

    override fun onJoinChannel(result: Int, channelId: Long, elapsed: Long, l2: Long) {
        Log.i(TAG, "onJoinChannel result: $result channelId: $channelId elapsed: $elapsed")
        if (result == NERtcConstants.ErrorCode.OK) {
            MainScope().launch {
                delay(2000) // 延时2秒
                mStatus?.text = "你可以开始说话了"
                mJoinChannel = true
                mRecordTag?.visibility = View.VISIBLE;
                mRecordTag?.playAnimation();
            }
        }
    }

    override fun onLeaveChannel(result: Int) {
        Log.i(TAG, "onLeaveChannel result: $result")
        finish()
    }

    @Deprecated("Deprecated in Java")
    override fun onUserJoined(userId: Long) {
        Log.i(TAG, "onUserJoined userId: $userId ....$mUserId")
        if (userId == gptId) {
            mGptview?.playAnimation();
            mGptview?.setBackgroundResource(R.drawable.circle_border);
        }
    }

    override fun onUserJoined(uid: Long, joinExtraInfo: NERtcUserJoinExtraInfo?) {
        Log.i(TAG, "onUserJoined uid: $uid")

    }

    @Deprecated("Deprecated in Java")
    override fun onUserLeave(userId: Long, i: Int) {
        Log.i(TAG, "onUserLeave uid: $userId ；；；；；$mUserId")
        if (userId == gptId) {
            mGptview?.cancelAnimation();
            mGptview?.setBackgroundResource(R.drawable.circle_border_offline);
            mStatus?.text = "当前服务器断开，请重试"
            mStatus?.setTextColor(resources.getColor(R.color.red))
        }
    }


    override fun onUserLeave(uid: Long, reason: Int, leaveExtraInfo: NERtcUserLeaveExtraInfo?) {}

    override fun onUserAudioStart(userId: Long) {
        Log.i(TAG, "onUserAudioStart uid: $userId")
        if (userId == gptId) {
            gptCALL = true;
            mStatus?.text = "我在听"
        }
        NERtcEx.getInstance().subscribeRemoteAudioStream(userId, true)
    }

    override fun onUserAudioStop(userId: Long) {
        Log.i(TAG, "onUserAudioStop uid: $userId")
        NERtcEx.getInstance().subscribeRemoteAudioStream(userId, false)
    }

    override fun onUserVideoStart(userId: Long, profile: Int) {
    }

    override fun onUserVideoStop(userId: Long) {
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
        if (id == R.id.image_close) {
            finish();
        }
    }

    /**
     * 网络状态枚举
     */
    enum class NetQuality(private val num: Int, private val msg: String) {
        UNKNOWN(0, "未知"), EXCELLENT(1, "非常好"), GOOD(2, "好"), POOR(3, "不太好"), BAD(
            4, "差"
        ),
        VERYBAD(5, "非常差"), DOWN(6, "无网络");

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