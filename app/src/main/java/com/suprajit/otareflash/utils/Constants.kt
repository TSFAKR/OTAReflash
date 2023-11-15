package com.suprajit.otareflash.utils

object Constants {
    const val TAG = "TSF_APPS"
    const val PREF_NAME = "Cluster_APP"
    const val BLE_CONNECTION_KEY = "BLE Connection"
    const val DND_STATE_KEY = "DND state"
    const val VEHICLE_TYPE_KEY = "Vehicle Type"
    const val AUTO_REPLY_STATE_KEY = "Auto Reply State"
    const val AUTO_REPLY_TEXT = "Auto Reply Text"
    const val DEFAULT_AUTO_REPLY_TEXT = "I am riding."

    //BLE Constants
//    const val NOTIFICATION_SERVICE_UUID = "49535343-fe7d-4ae5-8fa9-9fafd205e455"
//    const val NOTIFICATION_WRITE_UUID = "49535343-8841-43f4-a8d4-ecbe34729bb3"
//    const val RIDE_DETAILS_READ_UUID = "49535343-1e4d-4bd9-ba61-23c647249616"
//    const val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"
    const val NOTIFICATION_SERVICE_UUID = "49535343-fe7d-4ae5-8fa9-9fafd205e455"
    const val NOTIFICATION_WRITE_UUID = "49535343-8841-43f4-a8d4-ecbe34729bb3"
//    const val RIDE_DETAILS_READ_UUID = "49535343-1e4d-4bd9-ba61-23c647249616"
    const val DESCRIPTOR_UUID = "00002902-0000-1000-8000-00805f9b34fb"
    //Whatsapp Notification Constants
    const val WP_PACKAGE_NAME = "com.whatsapp"
    const val WP_INTENT_ACTION = "com.embien.clusterapp.notificationListenerService"
    const val WP_MESSAGE_SENDER = "Message Sender"
    const val WP_MESSAGE_TEXT = "Message Text"
    const val WP_MESSAGE_SUMMARY = "Message Summary"
    const val WP_CALL_CALLER = "Caller Name"
    const val WP_CALL_SUMMARY = "Call Summary"
    const val WP_CALL_NOTIFICATION_REMOVED = "Call Notification Removed"
    const val WP_MSG_NOTIFICATION_REMOVED = "Msg Notification Removed"

    //Sms Constants
    const val SMS_INTENT_ACTION = "android.provider.Telephony.SMS_RECEIVED"
}