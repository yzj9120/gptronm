package com.example.gptroom

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.netease.lava.nertc.sdk.NERtcConstants
import com.netease.lava.nertc.sdk.NERtcEx

class AudioCallActivity : AppCompatActivity() {

    private val EXTRA_ROOM_ID = "extra_room_id"
    private val EXTRA_USER_ID = "extra_user_id"
    private val TAG = "AudioCallActivity"

    private var mRoomId: String? = null
    private var mUserId: Long = 0
    private var mEnableLocalAudio = true
    private var mJoinChannel = false
    private val mIsSpeakerPhone = true

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
        mRoomId = intent.getStringExtra(EXTRA_ROOM_ID)
        mUserId = intent.getLongExtra(EXTRA_USER_ID, -1)
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
        mBackIv.setOnClickListener(this)
        mAudioRouteBtn.setOnClickListener(this)
        mMuteMicBtn.setOnClickListener(this)
        mHangUpBtn.setOnClickListener(this)
        if (!TextUtils.isEmpty(mRoomId)) {
            mRoomTittleTv.setText("房间号:$mRoomId")
        }
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
}