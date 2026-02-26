package com.meditech.hemav.util

import android.content.Context
import org.jitsi.meet.sdk.JitsiMeetActivity
import org.jitsi.meet.sdk.JitsiMeetConferenceOptions
import org.jitsi.meet.sdk.JitsiMeetUserInfo
import java.net.URL

object VideoCallManager {
    fun startCall(context: Context, roomName: String, displayName: String = "User") {
        try {
            val serverUrl = URL("https://meet.jit.si")
            
            val userInfo = JitsiMeetUserInfo()
            userInfo.displayName = displayName

            val options = JitsiMeetConferenceOptions.Builder()
                .setServerURL(serverUrl)
                .setRoom(roomName)
                .setAudioMuted(false)
                .setVideoMuted(false)
                .setUserInfo(userInfo)
                .build()

            JitsiMeetActivity.launch(context, options)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
