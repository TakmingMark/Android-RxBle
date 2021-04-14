package com.example.origamilabs_rxble_android

import android.bluetooth.*
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.Context
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.TimeUnit


class BluetoothHelper(context: Context) {

    var bluetoothListener: BluetoothListener? = null

    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager =
            context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private var a2dpBluetoothProfile: BluetoothProfile? = null

    private var a2dpServiceListener: ServiceListener? = null
    private val a2dpServiceListenerSingle = Single.create<BluetoothProfile> { emitter ->
        if (a2dpBluetoothProfile != null)
            emitter.onSuccess(a2dpBluetoothProfile!!)

        if (a2dpServiceListener == null) {
            a2dpServiceListener = object : ServiceListener {
                override fun onServiceConnected(p0: Int, bluetoothProfile: BluetoothProfile?) {
                    Timber.d("Bluetooth service connected")
                    this@BluetoothHelper.a2dpBluetoothProfile = bluetoothProfile
                    emitter.onSuccess(a2dpBluetoothProfile!!)
                }

                override fun onServiceDisconnected(p0: Int) {
                    Timber.d("Bluetooth service disconnected")
                    a2dpBluetoothProfile = null
                }
            }
            bluetoothAdapter.getProfileProxy(context, a2dpServiceListener, BluetoothProfile.A2DP)
        }
    }

    private var a2dpServiceListenerSingleDisposable: Disposable? = null
    private var checkA2dpConnectedTimerDisposable: Disposable? = null

    fun bondDevice(device: BluetoothDevice) {
        device.createBond()
    }

    fun removeBondDevice(device: BluetoothDevice){
        try {
            Timber.d("Try to remove bond from device")
            device::class.java.getMethod("removeBond").invoke(device)
        } catch (e: Exception) {
            Timber.d("Removing bond has been failed. ${e.message}")
        }
    }

    fun connectA2dp(device: BluetoothDevice) {
        a2dpServiceListenerSingle
            .subscribe { a2dpBluetoothProfile ->
                try {
                    Timber.d("Try to call Bluetooth priority function")
                    val setPriorityMethod =
                        BluetoothA2dp::class.java.getMethod(
                            "setPriority",
                            BluetoothDevice::class.java,
                            Int::class.javaPrimitiveType
                        )
                    setPriorityMethod.invoke(a2dpBluetoothProfile, device, 100)
                    Timber.d("Call Bluetooth a2dp priority function success!")
                } catch (ex: NoSuchMethodException) {
                    Timber.d("Unable to find setPriority(BluetoothDevice) method in BluetoothA2dp proxy.")
                } catch (e: IllegalAccessException) {
                    Timber.d("Illegal Access! $e")
                } catch (e: InvocationTargetException) {
                    Timber.d(
                        "Unable to invoke setPriority(BluetoothDevice) method on proxy. %s",
                        e.toString()
                    )
                }

                try {
                    Timber.d("Try to call Bluetooth A2DP connect function")
                    val connectMethod =
                        BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
                    connectMethod.invoke(a2dpBluetoothProfile, device)
                    Timber.d("Call Bluetooth a2dp connect function success!")
                } catch (ex: NoSuchMethodException) {
                    Timber.d("Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.")
                } catch (e: IllegalAccessException) {
                    Timber.d("Illegal Access! $e")
                } catch (e: InvocationTargetException) {
                    Timber.d(
                        "Unable to invoke connect(BluetoothDevice) method on proxy. %s",
                        e.toString()
                    )
                }
            }
            .apply {
                a2dpServiceListenerSingleDisposable = this
            }
    }

    fun checkA2dpConnectedTimer() {
        disposeCheckA2dpConnectedTimer()

        Single.create<Boolean> {
            it.onSuccess(isA2dpConnected())
        }.repeatWhen { completed ->
            completed.delay(2, TimeUnit.SECONDS)
        }
            .subscribe { isA2dpConnected ->
                Timber.d("A2DP connected:$isA2dpConnected")
                if (isA2dpConnected){
                    bluetoothListener?.onA2dpConnected()
                    checkA2dpConnectedTimerDisposable?.dispose()
                }
            }
            .apply {
                checkA2dpConnectedTimerDisposable = this
            }
    }

    fun disposeCheckA2dpConnectedTimer() {
        if (checkA2dpConnectedTimerDisposable != null) {
            checkA2dpConnectedTimerDisposable?.dispose()
            checkA2dpConnectedTimerDisposable = null
        }
    }

    fun isBondedDevice(device: BluetoothDevice): Boolean {
        return device.bondState==BluetoothDevice.BOND_BONDED
    }

    fun isA2dpConnected(): Boolean {
        return bluetoothAdapter.getProfileConnectionState(BluetoothA2dp.A2DP) == BluetoothA2dp.STATE_CONNECTED
    }

    interface IBluetoothListener {
        fun onA2dpConnected()
    }
}