package com.secureseco.trustsecoandroid

import android.content.Intent
import android.content.pm.PackageManager
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.secureseco.trustsecoandroid.api.TrustScoreApiService

class AppNotificationListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)

        // Checking if the notification is from the package installer and has keywords suggesting an app installation.
        val packageName = sbn?.packageName
        val tickerText = sbn?.notification?.tickerText?.toString() ?: ""

        if (packageName == "com.android.packageinstaller" || packageName == "com.google.android.packageinstaller") {
            if (tickerText.contains("installed", ignoreCase = true)) {
                val installedPackageName = getLastInstalledAppPackage() ?: ""; // Remember to replace this.
                fetchTrustScoreForApp(installedPackageName)
            }
        }
    }

    private fun fetchTrustScoreForApp(appPackageName: String) {
        val intent = Intent("com.secureseco.trustsecoandroid.NEW_APP_INSTALLED")
        intent.putExtra("packageName", appPackageName)
        sendBroadcast(intent)
    }

    private fun getLastInstalledAppPackage(): String? {
        val pm = applicationContext.packageManager

        // TODO: ask klimentina the android expert how to add permissions for this
        val packages = pm.getInstalledPackages(PackageManager.GET_META_DATA)

        // Sort the packages by their install time in descending order
        val sortedPackages = packages.sortedByDescending { it.firstInstallTime }

        return sortedPackages.getOrNull(0)?.packageName
    }


    // to be used later when we fix the backend
    suspend fun fetchTrustScore(packageName: String, version: String): Int? {
        val apiService = TrustScoreApiService.create()

        val response = apiService.getTrustScore(packageName, version)

        if (response.isSuccessful) {
            return response.body()?.trustScore
        } else {
            // You can also handle different types of errors here
            throw Exception("Failed to fetch trust score")
        }
    }
}