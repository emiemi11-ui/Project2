package com.vitanova.app

import android.app.Application
import com.vitanova.app.data.local.VitaNovaDatabase

class VitaNovaApp : Application() {

    val database: VitaNovaDatabase by lazy {
        VitaNovaDatabase.getInstance(this)
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }

    companion object {
        @Volatile
        private var instance: VitaNovaApp? = null

        fun getInstance(): VitaNovaApp =
            instance ?: throw IllegalStateException(
                "VitaNovaApp has not been created yet. Ensure it is declared in AndroidManifest.xml."
            )
    }
}
