package com.example.mensajeria

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.telephony.SmsManager
import android.telephony.TelephonyManager
import android.util.Log

class CallReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // 1. Verificamos que la acción sea la correcta
        if (intent.action != TelephonyManager.ACTION_PHONE_STATE_CHANGED) return

        val state = intent.getStringExtra(TelephonyManager.EXTRA_STATE)

        if (state == TelephonyManager.EXTRA_STATE_RINGING) {
            // 2. IMPORTANTE: En dispositivos físicos, EXTRA_INCOMING_NUMBER
            // a veces llega nulo si no tienes el permiso READ_CALL_LOG concedido.
            val incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER)

            val prefs = context.getSharedPreferences("ConfigSMS", Context.MODE_PRIVATE)
            val targetNumber = prefs.getString("num", "") ?: ""
            val autoMessage = prefs.getString("msg", "") ?: ""

            Log.d("AutoSMS", "Llamada detectada de: $incomingNumber")

            // 3. Comparación flexible: Limpiamos espacios y usamos 'endsWith'
            // para ignorar prefijos como +52 o 01.
            if (incomingNumber != null && targetNumber.isNotEmpty()) {
                val cleanTarget = targetNumber.trim()

                if (incomingNumber.endsWith(cleanTarget)) {
                    try {
                        val smsManager: SmsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            context.getSystemService(SmsManager::class.java)!!
                        } else {
                            @Suppress("DEPRECATION")
                            SmsManager.getDefault()
                        }

                        smsManager.sendTextMessage(incomingNumber, null, autoMessage, null, null)
                        Log.d("AutoSMS", "SMS enviado con éxito a $incomingNumber")
                    } catch (e: Exception) {
                        Log.e("AutoSMS", "Error al enviar SMS: ${e.message}")
                    }
                } else {
                    Log.d("AutoSMS", "El número no coincide con el configurado")
                }
            } else if (incomingNumber == null) {
                Log.e("AutoSMS", "Número entrante nulo. Revisa el permiso READ_CALL_LOG en los ajustes del cel.")
            }
        }
    }
}