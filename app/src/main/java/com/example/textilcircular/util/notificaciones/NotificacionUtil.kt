package com.example.textilcircular.util.notificaciones

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.textilcircular.MainActivity
import com.example.textilcircular.R

// Utilidad para emitir notificaciones locales cuando entra un nuevo mensaje.
// TODO: cuando haya servidor, las notificaciones llegarán con push.
object NotificacionUtil {

    private const val CANAL_MENSAJES_ID = "canal_mensajes_textil_circular"
    private const val CANAL_MENSAJES_NOMBRE = "Mensajes y Consultas"

    fun mostrarNotificacionMensaje(
        contexto: Context,
        titulo: String,
        contenido: String,
        conversacionId: Int
    ) {
        val notificationManager = contexto.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canal = NotificationChannel(
                CANAL_MENSAJES_ID,
                CANAL_MENSAJES_NOMBRE,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificaciones de nuevos mensajes en el chat"
            }
            notificationManager.createNotificationChannel(canal)
        }

        val intent = Intent(contexto, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("conversacionId", conversacionId)
        }

        val pendingIntent = PendingIntent.getActivity(
            contexto,
            conversacionId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notificacion = NotificationCompat.Builder(contexto, CANAL_MENSAJES_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(titulo)
            .setContentText(contenido)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(conversacionId, notificacion)
    }
}
