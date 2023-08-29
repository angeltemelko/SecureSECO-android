package com.secureseco.trustsecoandroid

import android.app.AlertDialog
import android.content.BroadcastReceiver
import com.secureseco.trustsecoandroid.viewmodel.TrustScoreViewModel
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.provider.Settings
import android.text.TextUtils
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

class MainActivity : ComponentActivity() {

    private val viewModel: TrustScoreViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isNotificationServiceEnabled()) {
            // If the user hasn't granted the permission, guide them to the settings page.
            val intent = Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            startActivity(intent)
        }

        setContent {
            MainScreen(viewModel)
        }

        // If we have an intent containing package info, fetch the score and show the dialog.
        intent.getStringExtra("packageName")?.let {
            viewModel.fetchTrustScoreForApp(it)
            showDialogWithTrustScore(viewModel.trustScore.value)
        }
    }

    @Composable
    fun MainScreen(viewModel: TrustScoreViewModel) {
        val trustScore by viewModel.trustScore.collectAsState()

        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "TrustSECO", style = MaterialTheme.typography.headlineMedium, modifier = Modifier.padding(16.dp))

                Spacer(modifier = Modifier.height(24.dp))

                Text(text = "This app checks the trust score of newly installed apps.", modifier = Modifier.padding(16.dp))

                Spacer(modifier = Modifier.height(16.dp))

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(Color.LightGray),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(text = "Trust Score: ${trustScore ?: "Not able to fetch score"}", modifier = Modifier.padding(16.dp))
                }
            }
        }
    }

    private fun isNotificationServiceEnabled(): Boolean {
        val pkgName = packageName
        val flat = Settings.Secure.getString(contentResolver, "enabled_notification_listeners")
        if (!TextUtils.isEmpty(flat)) {
            val names = flat.split(":").toTypedArray()
            for (name in names) {
                val cn = ComponentName.unflattenFromString(name)
                if (cn != null && TextUtils.equals(pkgName, cn.packageName)) {
                    return true
                }
            }
        }
        return false
    }

    private val newAppInstalledReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val packageName = intent?.getStringExtra("packageName")
            packageName?.let {
                val mainIntent = Intent(context, MainActivity::class.java)
                mainIntent.putExtra("packageName", it)
                mainIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(mainIntent)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(newAppInstalledReceiver, IntentFilter("com.secureseco.trustsecoandroid.NEW_APP_INSTALLED"))
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(newAppInstalledReceiver)
    }

    private fun showDialogWithTrustScore(score: String?) {
        AlertDialog.Builder(this)
            .setTitle("Trust Score")
            .setMessage(score ?: "Unknown")
            .setPositiveButton("Okay") { dialog, _ -> dialog.dismiss() }
            .show()
    }
}
