package com.example.origamilabs_rxble_android

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothManager
import android.content.Context
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.*

class BleHelper(context: Context) {
    var bleListener: IBleListener? = null

    private var serviceUuid: UUID = UUID.fromString("0000180d-0000-1000-8000-00805f9b34fb")
    private var characteristicUuid: UUID = UUID.fromString("00002a37-0000-1000-8000-00805f9b34fb")

    private val rxBleClient: RxBleClient by lazy {
        RxBleClient.create(context)
    }

    var isObserveBleStateEnabled = false
        private set
    private var bleStateSubscriptionDisposable: Disposable? = null
    var isScanEnabled = false
        private set
    private var scanSubscriptionDisposable: Disposable? = null
    var isConnectBleEnabled = false
        private set
    private var connectBleSubscriptionDisposable: Disposable? = null
    var isDiscoverServiceEnabled = false
        private set
    private var discoverServiceSubscriptionDisposable: Disposable? = null
    var isReadCharacteristicEnabled = false
        private set
    private var readCharacteristicValueSubscriptionDisposable: Disposable? = null
    var isListenNotificationEnabled = false
        private set
    private var listenNotificationDisposable: Disposable? = null

    private var macAddress = "6F:66:6C:6F:04:18"
    private var rxBleConnection: RxBleConnection? = null

    fun enabledObserveBleState(enabled: Boolean): Boolean {
        if (isObserveBleStateEnabled != enabled) {
            if (enabled)
                observeBleState()
            else
                disposeObserveBleState()
        }

        isObserveBleStateEnabled = enabled
        return isObserveBleStateEnabled
    }

    private fun observeBleState() {
        rxBleClient
            .observeStateChanges()
            .subscribe({ state ->
                when (state) {
                    RxBleClient.State.READY -> {
                        Timber.d("RxBleClient.State.READY")
                    }
                    RxBleClient.State.BLUETOOTH_NOT_AVAILABLE -> {
                        Timber.d("RxBleClient.State.BLUETOOTH_NOT_AVAILABLE")
                    }
                    RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED -> {
                        Timber.d("RxBleClient.State.LOCATION_PERMISSION_NOT_GRANTED")
                    }
                    RxBleClient.State.BLUETOOTH_NOT_ENABLED -> {
                        Timber.d("RxBleClient.State.BLUETOOTH_NOT_ENABLED")
                    }
                    RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED -> {
                        Timber.d("RxBleClient.State.LOCATION_SERVICES_NOT_ENABLED")
                    }
                    else -> {
                        Timber.d("RxBleClient.State.else")
                    }
                }
                bleListener?.onObserveBleState(state.name)
            }, { throwable ->
                bleListener?.onObserveBleStateError(throwable.toString())
                Timber.d(throwable)
            })
            .apply {
                bleStateSubscriptionDisposable = this
            }

        isObserveBleStateEnabled = true
    }

    private fun disposeObserveBleState() {
        if (bleStateSubscriptionDisposable != null) {
            bleStateSubscriptionDisposable?.dispose()
            bleStateSubscriptionDisposable = null
            isObserveBleStateEnabled = false
        }
    }

    fun enabledScan(enabled: Boolean): Boolean {
        if (isScanEnabled != enabled) {
            if (enabled)
                scan()
            else
                disposeScan()
        }

        isScanEnabled = enabled
        return isScanEnabled
    }

    private fun scan() {
        rxBleClient
            .scanBleDevices(
                com.polidea.rxandroidble2.scan.ScanSettings
                    .Builder()
                    .setScanMode(com.polidea.rxandroidble2.scan.ScanSettings.SCAN_MODE_LOW_LATENCY)
                    .build(),
                com.polidea.rxandroidble2.scan.ScanFilter
                    .Builder()
                    .build()
            )
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ scanResult ->
                if (!scanResult.bleDevice.name.isNullOrEmpty()) {
                    bleListener?.onScan(
                        scanResult.bleDevice.macAddress,
                        scanResult.bleDevice.name!!,
                        scanResult.rssi
                    )
                    Timber.d(scanResult.toString())
                }
            }, { throwable ->
                Timber.d(throwable)
                bleListener?.onScanError(throwable.toString())
            })
            .apply {
                scanSubscriptionDisposable = this
            }
        isScanEnabled = true
    }

    private fun disposeScan() {
        if (scanSubscriptionDisposable != null) {
            scanSubscriptionDisposable?.dispose()
            scanSubscriptionDisposable = null
            isScanEnabled = false
        }
    }


    fun enabledConnectBle(enabled: Boolean): Boolean {
        if (macAddress.isEmpty())
            return isConnectBleEnabled

        if (isConnectBleEnabled != enabled) {
            if (enabled)
                connectBle(macAddress)
            else
                disposeConnectBle()
        }

        isConnectBleEnabled = enabled
        return isConnectBleEnabled
    }

    private fun connectBle(macAddress: String) {
        val rxBleDevice = rxBleClient.getBleDevice(macAddress)

        rxBleDevice
            .establishConnection(false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ rxBleConnection ->
                this.rxBleConnection = rxBleConnection
                bleListener?.onConnectBle(macAddress)
            }, { throwable ->
                Timber.d(throwable)
                bleListener?.onConnectBleError(throwable.toString())
            })
            .apply {
                connectBleSubscriptionDisposable = this
            }

        isConnectBleEnabled = true
    }

    private fun disposeConnectBle() {
        if (connectBleSubscriptionDisposable != null) {
            connectBleSubscriptionDisposable?.dispose()
            connectBleSubscriptionDisposable = null
            isConnectBleEnabled = false
        }
    }

    fun enabledDiscoverService(enabled: Boolean): Boolean {
        if (rxBleConnection == null)
            return isDiscoverServiceEnabled

        if (isDiscoverServiceEnabled != enabled) {
            if (enabled)
                discoverService(rxBleConnection!!)
            else
                disposeDiscoverService()
        }

        isDiscoverServiceEnabled = enabled
        return isDiscoverServiceEnabled
    }

    private fun discoverService(rxBleConnection: RxBleConnection) {
        rxBleConnection
            .discoverServices()
            .flatMap { services ->
                services.getService(serviceUuid)
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ service ->
                if (service != null) {
                    bleListener?.onDiscoverBleService(serviceUuid)
                }
            }, { throwable ->
                Timber.d(throwable)
                bleListener?.onDiscoverBleServiceError(throwable.toString())
            })
            .apply {
                discoverServiceSubscriptionDisposable = this
            }
        isDiscoverServiceEnabled = true
    }

    private fun disposeDiscoverService() {
        if (discoverServiceSubscriptionDisposable != null) {
            discoverServiceSubscriptionDisposable?.dispose()
            discoverServiceSubscriptionDisposable = null
            isDiscoverServiceEnabled = false
        }
    }

    fun enabledReadBleCharacteristicValue(enabled: Boolean): Boolean {
        if (rxBleConnection == null)
            return isReadCharacteristicEnabled

        if (isReadCharacteristicEnabled != enabled) {
            if (enabled)
                readBleCharacteristicValue(rxBleConnection!!)
            else
                disposeReadBleCharacteristicValue()
        }

        isReadCharacteristicEnabled = enabled
        return isReadCharacteristicEnabled
    }

    private fun readBleCharacteristicValue(rxBleConnection: RxBleConnection) {
        rxBleConnection
            .readCharacteristic(characteristicUuid)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                { characteristicValue ->
                    if (characteristicValue != null) {
                        Timber.d(characteristicValue.toString())
                        bleListener?.onReadBleCharacteristicValue(characteristicValue.contentToString())
                    }
                },
                { throwable ->
                    Timber.d(throwable)
                    bleListener?.onReadBleCharacteristicValueError(throwable.toString())
                }
            )
            .apply {
                readCharacteristicValueSubscriptionDisposable = this
            }
        isReadCharacteristicEnabled = true
    }

    private fun disposeReadBleCharacteristicValue() {
        if (readCharacteristicValueSubscriptionDisposable != null) {
            readCharacteristicValueSubscriptionDisposable?.dispose()
            readCharacteristicValueSubscriptionDisposable = null
            isReadCharacteristicEnabled = false
        }
    }

    fun enabledListenNotification(enabled: Boolean): Boolean {
        if (rxBleConnection == null)
            return isListenNotificationEnabled

        if (isListenNotificationEnabled != enabled) {
            if (enabled)
                listenNotification(rxBleConnection!!)
            else
                disposeListenNotification()
        }

        isListenNotificationEnabled = enabled
        return isListenNotificationEnabled
    }

    private fun listenNotification(rxBleConnection: RxBleConnection) {
        rxBleConnection
            .setupNotification(characteristicUuid)
            .flatMap {
                it
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bytes ->
                bleListener?.onListenNotification(characteristicUuid, bytes[1].toInt())
            }, { throwable ->
                Timber.d(throwable)
                bleListener?.onListenNotificationError(throwable.toString())
            })
            .apply {
                listenNotificationDisposable = this
            }
        isListenNotificationEnabled = true
    }

    private fun disposeListenNotification() {
        if (listenNotificationDisposable != null) {
            listenNotificationDisposable?.dispose()
            listenNotificationDisposable = null
            isListenNotificationEnabled = false
        }
    }

    fun getBleDevice(): RxBleDevice? {
        if (macAddress.isEmpty())
            return null
        return rxBleClient.getBleDevice(macAddress)
    }

    fun getBluetoothDevice(): BluetoothDevice? {
        if (macAddress.isEmpty())
            return null
        return rxBleClient.getBleDevice(macAddress).bluetoothDevice
    }

    interface IBleListener {
        fun onObserveBleState(state: String)
        fun onObserveBleStateError(error: String)

        fun onScan(macAddress: String, deviceName: String, rssi: Int)
        fun onScanError(error: String)

        fun onConnectBle(macAddress: String)
        fun onConnectBleError(error: String)

        fun onDiscoverBleService(serviceUuid: UUID)
        fun onDiscoverBleServiceError(error: String)

        fun onReadBleCharacteristicValue(value: String)
        fun onReadBleCharacteristicValueError(error: String)

        fun onListenNotification(characteristicUuid: UUID, value: Int)
        fun onListenNotificationError(error: String)
    }
}