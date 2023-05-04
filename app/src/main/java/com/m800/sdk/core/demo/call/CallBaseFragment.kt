package com.m800.sdk.core.demo.call

import android.content.Context
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.PowerManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.lifecycle.*
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.m800.sdk.core.demo.fragment.BaseFragment
import com.m800.sdk.core.demo.navigation.UIManager
import com.m800.sdk.core.demo.navigation.Page
import com.m800.sdk.call.CinnoxAudioController
import com.m800.sdk.core.demo.utils.CustomManager
import com.m800.sdk.core.demo.utils.disableKeepScreenOn
import com.m800.sdk.core.demo.utils.enableKeepScreenOn
import com.m800.sdk.core.demo.R
import com.m800.sdk.core.demo.databinding.LcAudioRouteBottomSheetDialogBinding
import java.util.concurrent.TimeUnit

/**
 * Base fragment to handle call view behaviors:
 * 1. Setup proximity sensor to automatically turn off screen
 * 2. Switch turn on screen settings which relates to present call view when device screen off
 * **/
abstract class CallBaseFragment : BaseFragment() {
    companion object {
        private const val SCREEN_OFF_WAKE_LOCK_TAG_SUFFIX = ":screen_off"
        private val SCREEN_OFF_WAKE_LOCK_TIMEOUT = TimeUnit.HOURS.toMillis(4) // 4 hours
        private var isSensorEnabled = false

        private var currentPageList = arrayListOf<Page>()
    }

    private val TAG = CallBaseFragment::class.java.simpleName
    private lateinit var mAudioRouteBottomSheetBinding: LcAudioRouteBottomSheetDialogBinding
    private lateinit var audioRouteBottomSheetDialog: BottomSheetDialog

    private var isEnableScreenOffSensorByAudioRoute: Boolean = true
    private val audioController = CallController.getInstance().getAudioController()
    private var screenOffWakeLock: PowerManager.WakeLock? = null
    private var sensorManager: SensorManager? = null
    private var sensor: Sensor? = null
    private var isProximitySensorEnable = true
    private var isProximityNearEnable = false
    private var proximityListener = object : SensorEventListener {
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
            // Do nothing
        }

        override fun onSensorChanged(event: SensorEvent?) {
            if (!isSensorEnabled) {
                Log.w(TAG, "Sensor listener should be unregistered!!")
                sensorManager?.unregisterListener(this)
                setScreenOffWakeLock(false)
                return
            }

            if (!isEnableScreenOffSensorByAudioRoute) {
                Log.w(TAG, "setScreenOffWakeLock by audio route")
                setScreenOffWakeLock(false)
                return
            }

            if (event?.sensor?.type != Sensor.TYPE_PROXIMITY) {
                return
            }

            if (event.values[0] < event.sensor.maximumRange) {
                setScreenOffWakeLock(true)
            } else {
                setScreenOffWakeLock(false)
            }
        }
    }

    private val audioControllerListener = object : CinnoxAudioController.CinnoxAudioControllerListener {
        override fun onAudioRouteChanged(
            oldRoute: CinnoxAudioController.Route,
            newRoute: CinnoxAudioController.Route
        ) {
            Log.i(TAG, "onAudioRouteChanged newRoute: $newRoute ")
            isEnableScreenOffSensorByAudioRoute = getEnableScreenOffByAudioRoute(newRoute)
        }
    }
    
    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onCreate(owner: LifecycleOwner) {
                owner.lifecycle.removeObserver(this)

                activity?.let {
                    val currentPg = UIManager.getNavigator().getCurrentPage()
                    currentPg?.let { setCurrentPage(it) }
                    if (currentPageList.size == 1) {
                        enableKeepScreenOn(it)
                    }
                }
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        removeCurrentPage()
    }

    private fun setCurrentPage(page: Page) {
        Log.i(TAG, "setCurrentPage page: ${page}, currentPageList: ${currentPageList.joinToString()}")
        currentPageList.add(page)
    }

    private fun removeCurrentPage() {
        Log.i(TAG, "currentPageList: ${currentPageList.joinToString()}")
        if (currentPageList.size > 1) {
            currentPageList.remove(currentPageList.last())
        } else {
            activity?.let {
                disableKeepScreenOn(it)
            }
            currentPageList.clear()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (isProximitySensorEnable) {
            setupProximitySensor()
        }
        mAudioRouteBottomSheetBinding =
            LcAudioRouteBottomSheetDialogBinding.inflate(inflater, container, false)
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    fun setProximitySensorEnable(enable: Boolean) {
        isProximitySensorEnable = enable
    }

    override fun onPause() {
        super.onPause()
        isSensorEnabled = false
        sensorManager?.unregisterListener(proximityListener)
        audioController.unregisterAudioControllerListener(audioControllerListener)
        setScreenOffWakeLock(false)
    }

    override fun onResume() {
        super.onResume()
        isSensorEnabled = true
        sensorManager?.registerListener(
            proximityListener,
            sensor,
            SensorManager.SENSOR_DELAY_NORMAL
        )
        audioController.registerAudioControllerListener(audioControllerListener)
        isEnableScreenOffSensorByAudioRoute =
            getEnableScreenOffByAudioRoute(audioController.getCurrentRouting())
    }

    private fun getEnableScreenOffByAudioRoute(audioRoute: CinnoxAudioController.Route?): Boolean {
        return audioRoute == CinnoxAudioController.Route.EARPIECE
    }

    private fun setScreenOffWakeLock(newEnable: Boolean) {
        Log.i(TAG, "setScreenOffWakeLock newEnable: $newEnable")
        if (newEnable != isProximityNearEnable) {
            isProximityNearEnable = newEnable
            if (newEnable) {
                screenOffWakeLock?.acquire(SCREEN_OFF_WAKE_LOCK_TIMEOUT)
            } else {
                if (screenOffWakeLock?.isHeld == true) {
                    screenOffWakeLock?.release()
                }
            }
        }
    }

    private fun setupProximitySensor() {
        val appContext = activity?.applicationContext ?: run {
            Log.w(TAG, "Can't get application context")
            return
        }

        val tag = "${appContext.packageName}$SCREEN_OFF_WAKE_LOCK_TAG_SUFFIX"

        screenOffWakeLock =
            (appContext.getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,
                tag
            )
        sensorManager = appContext.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensor = sensorManager?.getDefaultSensor(Sensor.TYPE_PROXIMITY)
    }

    fun initAudioRouteBottomSheet(context: Context) {
        audioRouteBottomSheetDialog =
            BottomSheetDialog(context, R.style.TransparentBottomSheetDialog)
        val rootView = mAudioRouteBottomSheetBinding.root
        val phoneBtn = rootView.findViewById<TextView>(R.id.tv_phone)
        val speakerBtn = rootView.findViewById<TextView>(R.id.tv_speaker)
        val bluetoothBtn = rootView.findViewById<TextView>(R.id.tv_bluetooth)
        val wiredHeadsetBtn = rootView.findViewById<TextView>(R.id.tv_wired_headset)

        phoneBtn.setOnClickListener {
            audioController.switchToRoute(CinnoxAudioController.Route.EARPIECE)
            audioRouteBottomSheetDialog.dismiss()
        }

        speakerBtn.setOnClickListener {
            audioController.switchToRoute(CinnoxAudioController.Route.SPEAKER)
            audioRouteBottomSheetDialog.dismiss()
        }

        bluetoothBtn.setOnClickListener {
            audioController.switchToRoute(CinnoxAudioController.Route.BLUETOOTH)
            audioRouteBottomSheetDialog.dismiss()
        }

        wiredHeadsetBtn.setOnClickListener {
            audioController.switchToRoute(CinnoxAudioController.Route.WIRED_HEADSET)
            audioRouteBottomSheetDialog.dismiss()
        }

        audioRouteBottomSheetDialog.setContentView(rootView)
    }

    fun showAudioRouteBottomSheet() {
        if (!audioRouteBottomSheetDialog.isShowing) {
            clearTextViewState()

            val currentRouting = audioController.getCurrentRouting()
            val rootView = mAudioRouteBottomSheetBinding.root
            val phoneBtn = rootView.findViewById<TextView>(R.id.tv_phone)
            val speakerBtn = rootView.findViewById<TextView>(R.id.tv_speaker)
            val bluetoothBtn = rootView.findViewById<TextView>(R.id.tv_bluetooth)
            val wiredHeadsetBtn = rootView.findViewById<TextView>(R.id.tv_wired_headset)

            val currentRoutingList = audioController.getRoutingList()

            val currentBluetoothDeviceName = audioController.getCurrentBluetoothDeviceName()

            currentRoutingList.map {
                when (it) {
                    CinnoxAudioController.Route.EARPIECE -> {
                        phoneBtn.visibility = View.VISIBLE
                    }
                    CinnoxAudioController.Route.SPEAKER -> {
                        speakerBtn.visibility = View.VISIBLE
                    }
                    CinnoxAudioController.Route.BLUETOOTH -> {
                        bluetoothBtn.visibility = View.VISIBLE
                        bluetoothBtn.text = currentBluetoothDeviceName
                    }
                    CinnoxAudioController.Route.WIRED_HEADSET -> {
                        wiredHeadsetBtn.visibility = View.VISIBLE
                    }
                }
            }

            when (currentRouting) {
                CinnoxAudioController.Route.EARPIECE -> {
                    phoneBtn.setTextColor(CustomManager.getColorSecondary())
                    setTextViewDrawableColor(phoneBtn, CustomManager.getColorSecondary())
                }
                CinnoxAudioController.Route.SPEAKER -> {
                    speakerBtn.setTextColor(CustomManager.getColorSecondary())
                    setTextViewDrawableColor(speakerBtn, CustomManager.getColorSecondary())
                }
                CinnoxAudioController.Route.BLUETOOTH -> {
                    bluetoothBtn.setTextColor(CustomManager.getColorSecondary())
                    setTextViewDrawableColor(bluetoothBtn, CustomManager.getColorSecondary())
                }
                CinnoxAudioController.Route.WIRED_HEADSET -> {
                    wiredHeadsetBtn.setTextColor(CustomManager.getColorSecondary())
                    setTextViewDrawableColor(wiredHeadsetBtn, CustomManager.getColorSecondary())
                }
            }

            audioRouteBottomSheetDialog.show()
        }
    }

    private fun clearTextViewState() {
        val rootView = mAudioRouteBottomSheetBinding.root
        val phoneBtn = rootView.findViewById<TextView>(R.id.tv_phone)
        val speakerBtn = rootView.findViewById<TextView>(R.id.tv_speaker)
        val bluetoothBtn = rootView.findViewById<TextView>(R.id.tv_bluetooth)
        val wiredHeadsetBtn = rootView.findViewById<TextView>(R.id.tv_wired_headset)

        phoneBtn.setTextColor(resources.getColor(R.color.black))
        speakerBtn.setTextColor(resources.getColor(R.color.black))
        bluetoothBtn.setTextColor(resources.getColor(R.color.black))
        wiredHeadsetBtn.setTextColor(resources.getColor(R.color.black))
        setTextViewDrawableColor(phoneBtn, resources.getColor(R.color.black))
        setTextViewDrawableColor(speakerBtn, resources.getColor(R.color.black))
        setTextViewDrawableColor(bluetoothBtn, resources.getColor(R.color.black))
        setTextViewDrawableColor(wiredHeadsetBtn, resources.getColor(R.color.black))

        phoneBtn.visibility = View.GONE
        speakerBtn.visibility = View.GONE
        bluetoothBtn.visibility = View.GONE
        wiredHeadsetBtn.visibility = View.GONE
    }

    private fun setTextViewDrawableColor(textView: TextView, @ColorInt color: Int) {
        for (drawable in textView.compoundDrawables) {
            if (drawable != null) {
                drawable.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC_IN)
            }
        }
    }
}