package at.sysco.erp_connect.model

import android.content.Context
import android.net.ConnectivityManager

object ModelUtitlity {
    //Prüft ob File existiert
    fun doesFileExist(context: Context, string: String): Boolean {
        var doesExist = false
        if (context.fileList().contains(string)) {
            doesExist = true
        }
        return doesExist
    }

    //Prüft ob Internet-Verbindung besteht
    fun checkInternetConnection(context: Context): Boolean {
        var isConnected = false
        val connectivity =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val info = connectivity.allNetworks
        for (i in info.indices) {
            if (info[i] != null && connectivity.getNetworkInfo(info[i])!!.isConnected) {
                isConnected = true
            }
        }
        return isConnected
    }

    //Löscht file
    fun removeFile(context: Context, string: String) {
        when {
            doesFileExist(context, string) -> context.deleteFile(string)
        }
    }
}