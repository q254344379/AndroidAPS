package info.nightscout.androidaps.services

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import info.nightscout.androidaps.activities.ErrorHelperActivity
import info.nightscout.androidaps.interfaces.NotificationHolder
import info.nightscout.shared.logging.AAPSLogger
import info.nightscout.shared.logging.LTag
import javax.inject.Inject
import javax.inject.Singleton

/*
    This code replaces  following
    val alarm = Intent(context, AlarmSoundService::class.java)
    alarm.putExtra("soundId", n.soundId)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(alarm) else context.startService(alarm)

    it fails randomly with error
    Context.startForegroundService() did not then call Service.startForeground(): ServiceRecord{e317f7e u0 info.nightscout.nsclient/info.nightscout.androidaps.services.AlarmSoundService}

 */
@Singleton
class AlarmSoundServiceHelper @Inject constructor(
    private val aapsLogger: AAPSLogger,
    private val notificationHolder: NotificationHolder
) {

    fun startAlarm(context: Context, sound: Int) {
        aapsLogger.debug(LTag.CORE, "Starting alarm")
        val connection = object : ServiceConnection {
            override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
                // The binder of the service that returns the instance that is created.
                val binder: AlarmSoundService.LocalBinder = service as AlarmSoundService.LocalBinder

                val alarmSoundService: AlarmSoundService = binder.getService()

                context.startForegroundService(getServiceIntent(context, sound))

                // This is the key: Without waiting Android Framework to call this method
                // inside Service.onCreate(), immediately call here to post the notification.
                alarmSoundService.startForeground(notificationHolder.notificationID, notificationHolder.notification)

                // Release the connection to prevent leaks.
                context.unbindService(this)
            }

            override fun onServiceDisconnected(name: ComponentName?) {
            }
        }

        try {
            context.bindService(getServiceIntent(context, sound), connection, Context.BIND_AUTO_CREATE)
        } catch (ignored: RuntimeException) {
            // This is probably a broadcast receiver context even though we are calling getApplicationContext().
            // Just call startForegroundService instead since we cannot bind a service to a
            // broadcast receiver context. The service also have to call startForeground in
            // this case.
            context.startForegroundService(getServiceIntent(context, sound))
        }
    }

    fun stopService(context: Context) {
        aapsLogger.debug(LTag.CORE, "Stopping alarm")
        val alarm = Intent(context, AlarmSoundService::class.java)
        context.stopService(alarm)
    }

    private fun getServiceIntent(context: Context, sound: Int): Intent {
        val alarm = Intent(context, AlarmSoundService::class.java)
        alarm.putExtra(ErrorHelperActivity.SOUND_ID, sound)
        return alarm
    }
}