package com.singularity.ipcaplus.models

data class PushNotification(
    var data: NotificationData,
    var to: String,
)