package com.singularity.ipcaplus.utils

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.singularity.ipcaplus.R
import java.lang.Exception

const val channelId = ""
const val channelName = "com.singularity.ipcaplus"

class MyFirebaseMessagingService : FirebaseMessagingService() {

    //Here the application on receiving a message will check if its not empty and then it will handle the message by sending it to the context
    override fun onMessageReceived(remoteMessage: RemoteMessage) {


        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: ${remoteMessage.from}")

        // Check if message contains a data payload.
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(TAG, "Message data payload: ${remoteMessage.data}")

            if (/* Check if data needs to be processed by long running job */ true) {
                // For long-running tasks (10 seconds or more) use WorkManager.
                scheduleJob()
            } else {
                // Handle message within 10 seconds
                handleNow()
            }
            val clickAction = remoteMessage.data["click_action"]

            println("click Action")
            println(clickAction)

            //broadcastContentReady(applicationContext, remoteMessage.data["title"]!!, remoteMessage.data["content"]!!)
            sendNotification(remoteMessage.data["title"]!!, remoteMessage.data["content"]!!, clickAction!!)
        }


        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }

    //here the message will be sent back to the application
    private fun broadcastContentReady(context: Context, messageHead: String, messageBody: String) {
        val intent = Intent(BROADCAST_NEW_NOTIFICATION)
        try {
            intent.putExtra(NOTIFICATION_HEAD, messageHead)
            intent.putExtra(NOTIFICATION_BODY, messageBody)
            //sends the message to the corresponding context
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            //Log Message
        }
    }


    // [END receive_message]

    // [START on_new_token]
    /**
     * Called if the FCM registration token is updated. This may occur if the security of
     * the previous token had been compromised. Note that this is called when the
     * FCM registration token is initially generated so this is where you would retrieve the token.
     */
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token: $token")

        // If you want to send messages to this application instance or
        // manage this apps subscriptions on the server side, send the
        // FCM registration token to your app server.
        sendRegistrationToServer(token)
    }
    // [END on_new_token]

    /**
     * Schedule async work using WorkManager.
     */
    private fun scheduleJob() {
        // [START dispatch_job]
        //val work = OneTimeWorkRequest.Builder(MyWorker::class.java).build()
        //WorkManager.getInstance(this).beginWith(work).enqueue()
        // [END dispatch_job]
    }

    /**
     * Handle time allotted to BroadcastReceivers.
     */
    private fun handleNow() {
        Log.d(TAG, "Short lived task is done.")
    }

    /**
     * Persist token to third-party servers.
     *
     * Modify this method to associate the user's FCM registration token with any server-side account
     * maintained by your application.
     *
     * @param token The new token.
     */
    private fun sendRegistrationToServer(token: String?) {
        // TODO: Implement this method to send token to your app server.
        Log.d(TAG, "sendRegistrationTokenToServer($token)")
    }

    /**
     * Create and show a simple notification containing the received FCM message.
     *
     * @param messageBody FCM message body received.
     */


    @SuppressLint("RemoteViewLayout")
    fun getRemoteView(messageTitle : String, messageBody: String) : RemoteViews {
        val remoteView = RemoteViews("com.singularity.ipcaplus", R.layout.notification)

        remoteView.setTextViewText(R.id.title, messageTitle)
        remoteView.setTextViewText(R.id.message, messageBody)
        remoteView.setImageViewResource(R.id.app_logo, R.mipmap.ic_launcher_foreground)

        return remoteView
    }


    private fun sendNotification(messageTitle: String, messageBody: String, clickAction : String) {
        //val intent = Intent(this, MainActivity::class.java)

        val intent = Intent(clickAction)

        //I will implement this in the future
        //intent.putExtra("chat_id", chatId)

        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        val pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
            PendingIntent.FLAG_ONE_SHOT)


        //Defining the sound that the notification makes when appearing
        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        //Configuring the notification
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.chat_photo)
            .setContentTitle(messageTitle)
            .setContentText(messageBody)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)

        notificationBuilder.setContent(getRemoteView(messageTitle, messageBody))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Since android Oreo notification channel is needed.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH)
            channel.setShowBadge(true)
            notificationManager.createNotificationChannel(channel)
        }

        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build())
    }

    companion object {

        private const val TAG = "MyFirebaseMsgService"
        const val BROADCAST_NEW_NOTIFICATION = "com.singularity.ipcaplus.notification"
        const val NOTIFICATION_BODY = "com.singularity.ipcaplus.notification.body"
        const val NOTIFICATION_HEAD = "com.singularity.ipcaplus.notification.head"
    }
}