package net.wearpipe.app

import android.app.Application

class WearPipeApplication : Application() {
    val graph: AppGraph by lazy { AppGraph(this) }
    override fun onCreate() {
        super.onCreate()
        graph
    }
}
