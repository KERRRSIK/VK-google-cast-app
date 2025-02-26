package com.example.vk_google_cast_app

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaLoadRequestData
import com.google.android.gms.cast.framework.CastContext
import com.google.android.gms.cast.framework.CastSession
import com.google.android.gms.cast.framework.SessionManager
import com.google.android.gms.cast.framework.SessionManagerListener

class MainActivity : AppCompatActivity() {

    private lateinit var sessionManager: SessionManager
    private var castSession: CastSession? = null

    private val videoUrl =
        "https://videolink-test.mycdn.me/?pct=1&sig=6QNOvp0y3BE&ct=0&clientType=45&mid=193241622673&type=5"

    private val sessionListener = object : SessionManagerListener<CastSession> {
        override fun onSessionStarted(session: CastSession, sessionId: String) {
            castSession = session
        }

        override fun onSessionEnded(session: CastSession, error: Int) {
            castSession = null
        }

        override fun onSessionResumed(session: CastSession, wasSuspended: Boolean) {
            castSession = session
        }

        override fun onSessionResumeFailed(session: CastSession, error: Int) {
            castSession = null
        }

        override fun onSessionSuspended(session: CastSession, reason: Int) {}
        override fun onSessionStarting(session: CastSession) {}
        override fun onSessionResuming(session: CastSession, sessionId: String) {}
        override fun onSessionEnding(session: CastSession) {}
        override fun onSessionStartFailed(p0: CastSession, p1: Int) {}
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val castContext = CastContext.getSharedInstance(this.applicationContext)
        sessionManager = castContext.sessionManager
        castSession = sessionManager.currentCastSession

        val castButton = findViewById<Button>(R.id.cast_button)
        castButton.setOnClickListener {
            castVideo()
        }
    }

    private fun castVideo() {
        castSession = sessionManager.currentCastSession
        val session = castSession ?: run {
            showToast("No active cast session")
            return
        }

        val remoteMediaClient = session.remoteMediaClient ?: run {
            showToast("Failed to get RemoteMediaClient")
            return
        }

        val mediaInfo = MediaInfo.Builder(videoUrl)
            .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
            .setContentType("video/mp4")
            .build()

        val mediaLoadRequestData = MediaLoadRequestData.Builder()
            .setMediaInfo(mediaInfo)
            .build()

        // setResultCallback для обработки результата загрузки
        remoteMediaClient.load(mediaLoadRequestData)
            .setResultCallback { result ->
                if (result.status.isSuccess) {
                    showToast("Video started casting")
                } else {
                    showToast("Error casting video: ${result.status.statusMessage}")
                }
            }
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        super.onStart()
        sessionManager.addSessionManagerListener(sessionListener, CastSession::class.java)
    }

    override fun onStop() {
        super.onStop()
        sessionManager.removeSessionManagerListener(sessionListener, CastSession::class.java)
    }
}
