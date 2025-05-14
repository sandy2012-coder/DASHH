package com.example.backgroundvideorecorder

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.*
import android.hardware.camera2.*
import android.media.MediaRecorder
import android.os.*
import android.util.Log
import androidx.core.app.NotificationCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class VideoRecordingService : Service() {
    private lateinit var cameraDevice: CameraDevice
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var cameraCaptureSession: CameraCaptureSession
    private lateinit var cameraManager: CameraManager

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action
        if (action == "START") {
            startForeground(1, createNotification())
            startBackgroundRecording()
        } else if (action == "STOP") {
            stopRecording()
            stopSelf()
        }
        return START_STICKY
    }

    private fun startBackgroundRecording() {
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        val cameraId = cameraManager.cameraIdList[0]
        val handler = Handler(Looper.getMainLooper())

        cameraManager.openCamera(cameraId, object : CameraDevice.StateCallback() {
            override fun onOpened(device: CameraDevice) {
                cameraDevice = device
                startMediaRecorder()
            }
            override fun onDisconnected(device: CameraDevice) {
                device.close()
            }
            override fun onError(device: CameraDevice, error: Int) {
                device.close()
            }
        }, handler)
    }

    private fun startMediaRecorder() {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val file = File(getExternalFilesDir(null), "video_$timestamp.mp4")
        mediaRecorder = MediaRecorder()
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setOutputFile(file.absolutePath)
        mediaRecorder.setVideoEncodingBitRate(10000000)
        mediaRecorder.setVideoFrameRate(30)
        mediaRecorder.setVideoSize(1920, 1080)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        mediaRecorder.prepare()

        val recorderSurface = mediaRecorder.surface
        val captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD)
        captureRequestBuilder.addTarget(recorderSurface)

        cameraDevice.createCaptureSession(listOf(recorderSurface), object : CameraCaptureSession.StateCallback() {
            override fun onConfigured(session: CameraCaptureSession) {
                cameraCaptureSession = session
                session.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                mediaRecorder.start()
            }

            override fun onConfigureFailed(session: CameraCaptureSession) {
                Log.e("VideoService", "CaptureSession configuration failed")
            }
        }, null)
    }

    private fun stopRecording() {
        try {
            mediaRecorder.stop()
            mediaRecorder.reset()
            cameraCaptureSession.close()
            cameraDevice.close()
        } catch (e: Exception) {
            Log.e("VideoService", "Stop failed: ${e.message}")
        }
    }

    private fun createNotification(): Notification {
        val notificationChannelId = "VIDEO_REC_CHANNEL"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "Background Video Recording",
                NotificationManager.IMPORTANCE_LOW
            )
            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
                .createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Recording Video")
            .setContentText("Your video is being recorded in the background.")
            .setSmallIcon(android.R.drawable.ic_btn_speak_now)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}