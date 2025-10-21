package pt.iade.ei.xplored

import android.app.Application
import pt.iade.ei.xplored.models.PlaceRepository

class XploredApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PlaceRepository.initialize(this)
    }
}
