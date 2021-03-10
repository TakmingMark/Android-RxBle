package com.example.origamilabs_rxble_android

import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.bluetooth.BluetoothProfile.ServiceListener
import android.content.Context
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method


class BluetoothHelper(context: Context) {

    private var a2dpBluetoothProfile: BluetoothProfile? = null

    private var a2dpServiceListener: ServiceListener? = null
    private val a2dpServiceListenerSingle = Single.create<BluetoothProfile> { emitter ->
        if(a2dpBluetoothProfile!=null)
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
            val bluetoothManager =
                context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
            val bluetoothAdapter = bluetoothManager.adapter
            bluetoothAdapter.getProfileProxy(context, a2dpServiceListener, BluetoothProfile.A2DP)
        }
    }

    private var a2dpServiceListenerSingleDisposable:Disposable?=null

    fun bondDevice(device: BluetoothDevice) {
        device.createBond()
    }

    fun connectA2dp(device: BluetoothDevice) {
        a2dpServiceListenerSingle
            .subscribe {a2dpBluetoothProfile->
                try {
                    val setPriorityMethod =
                        BluetoothA2dp::class.java.getMethod(
                            "setPriority",
                            BluetoothDevice::class.java,
                            Int::class.javaPrimitiveType
                        )
                    setPriorityMethod.invoke(a2dpBluetoothProfile, device, 100)
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
                    val connectMethod =
                        BluetoothA2dp::class.java.getMethod("connect", BluetoothDevice::class.java)
                    connectMethod.invoke(a2dpBluetoothProfile, device)
                } catch (ex: NoSuchMethodException) {
                    Timber.d("Unable to find connect(BluetoothDevice) method in BluetoothA2dp proxy.")
                } catch (e: IllegalAccessException) {
                    Timber.d("Illegal Access! $e")
                } catch (e: InvocationTargetException) {
                    Timber.d("Unable to invoke connect(BluetoothDevice) method on proxy. %s", e.toString())
                }
            }
            .apply {
                a2dpServiceListenerSingleDisposable=this
            }
    }

}