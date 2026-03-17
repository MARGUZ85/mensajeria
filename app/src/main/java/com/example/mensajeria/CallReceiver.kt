package com.example.mensajeria

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            // El número entrante
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            // Cargar configuración
            val prefs = context.getSharedPreferences("ConfigSMS", Context.MODE_PRIVATE)
            val targetNumber = prefs.getString("num", "") ?: ""
            val autoMessage = prefs.getString("msg", "") ?: ""

            Log.d("AutoSMS", "Llamada de: $incomingNumber")

            if (incomingNumber != null && targetNumber.isNotEmpty() && incomingNumber.contains(targetNumber)) {
                try {
                    val smsManager = context.getSystemService(SmsManager::class.java)
                    smsManager.sendTextMessage(incomingNumber, null, autoMessage, null, null)
                    Log.d("AutoSMS", "SMS enviado con éxito")
                } catch (e: Exception) {
                    Log.e("AutoSMS", "Error al enviar SMS: ${e.message}")
                }
            }
        }
    }
}