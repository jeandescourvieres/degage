package com.degage.tile

import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import com.degage.R
import com.degage.prefs.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Tuile de réglages rapides : active/désactive la protection sans ouvrir l'application. */
class ProtectionTileService : TileService() {

    private val job = Job()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    private lateinit var prefs: AppPreferences

    override fun onCreate() {
        super.onCreate()
        prefs = AppPreferences(applicationContext)
    }

    override fun onStartListening() {
        super.onStartListening()
        scope.launch { updateTile(prefs.isEnabled.first()) }
    }

    override fun onClick() {
        super.onClick()
        scope.launch {
            val newValue = !prefs.isEnabled.first()
            prefs.setEnabled(newValue)
            updateTile(newValue)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }

    private fun updateTile(enabled: Boolean) {
        val tile = qsTile ?: return
        tile.state = if (enabled) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        tile.label = getString(R.string.app_name)
        tile.subtitle = if (enabled) "Protection ON" else "Protection OFF"
        tile.icon = android.graphics.drawable.Icon.createWithResource(this, R.drawable.ic_notification)
        tile.updateTile()
    }
}
