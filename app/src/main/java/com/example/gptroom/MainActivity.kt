package com.example.gptroom

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.gptroom.utils.DeviceUtils
import java.util.Random

class MainActivity : AppCompatActivity() {

    private var mJoinBtn: Button? = null
    private var mRoomIdEt: EditText? = null
    private var mUserIdEt: EditText? = null
    private var mBackIv: ImageView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        initViews()
    }


    private fun initViews() {
        val RoomId = "1382"
        mJoinBtn = findViewById(R.id.btn_join)
        mRoomIdEt = findViewById(R.id.room_id)
        mUserIdEt = findViewById(R.id.user_id)
        mRoomIdEt?.setText(RoomId)
        mUserIdEt?.setText(DeviceUtils.generateUUID())
        mBackIv?.setOnClickListener(View.OnClickListener { finish() })
        mJoinBtn?.setOnClickListener(View.OnClickListener {
            val roomIdEdit = mRoomIdEt?.getText()
            if (roomIdEdit.isNullOrEmpty()) {
                return@OnClickListener
            }
            val userIdEdit = mUserIdEt?.getText()
            if (userIdEdit.isNullOrEmpty()) {
                return@OnClickListener
            }
            AudioCallActivity.startActivity(
                this,
                roomIdEdit.toString(),
                userIdEdit.toString().toLong()
            )
        })
    }

}