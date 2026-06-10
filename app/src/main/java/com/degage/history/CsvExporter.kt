package com.degage.history

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.degage.database.entities.BlockedCallEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Exporte l'historique des appels bloqués en CSV et ouvre le menu de partage Android. */
fun exportCallsToCsv(context: Context, calls: List<BlockedCallEntity>) {
    val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.FRENCH)
    val sb = StringBuilder("Numéro;Date;Mode;Message\n")
    calls.forEach { call ->
        val date = sdf.format(Date(call.timestamp))
        val message = call.replyUsed.replace("\"", "'").replace(";", ",")
        sb.append("${call.phoneNumber};$date;${call.modeName};\"$message\"\n")
    }

    val dir = File(context.cacheDir, "exports").apply { mkdirs() }
    val file = File(dir, "tu_degages_historique.csv")
    FileOutputStream(file).use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }

    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/csv"
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, "Exporter l'historique"))
}
