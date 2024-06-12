package com.example.gptroom

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
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
        mUserIdEt?.setText(Random().nextInt(100000).toString())
        mBackIv?.setOnClickListener(View.OnClickListener { finish() })
        mJoinBtn?.setOnClickListener(View.OnClickListener {
            val roomIdEdit = mRoomIdEt?.getText()
            if (roomIdEdit == null || roomIdEdit.length <= 0) {
                return@OnClickListener
            }
            val userIdEdit = mUserIdEt?.getText()
            if (userIdEdit == null || userIdEdit.length <= 0) {
                return@OnClickListener
            }
//            startActivity(
//                this@VideoCallEntryActivity,
//                roomIdEdit.toString(),
//                userIdEdit.toString().toLong()
//            )
        })
    }

}