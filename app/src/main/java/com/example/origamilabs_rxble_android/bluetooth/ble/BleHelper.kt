package com.example.origamilabs_rxble_android.bluetooth.ble

import android.bluetooth.BluetoothDevice
import android.content.Context
import com.polidea.rxandroidble2.RxBleClient
import com.polidea.rxandroidble2.RxBleConnection
import com.polidea.rxandroidble2.RxBleDevice
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import timber.log.Timber
import java.util.*
import java.util.concurrent.TimeUnit


class BleHelper(context: Context) {
    private companion object {
        private const val AUTO_CLOSE_SCAN_TIME = 10L
    }

    var bleListener: IBleListener? = null

    private var device: BluetoothDevice? = null

    private val rxBleClient: RxBleClient by lazy {
        RxBleClient.create(context)
    }

    var isObserveBleStateEnabled = false
        private set
    private var bleStateSubscriptionDisposable: Disposable? = null
    var isScanEnabled = false
        private set
    private var scanSubscriptionDisposable: Disposable? = null
    private var autoCloseScanSubscriptionDisposable: Disposable? = null
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

    private var rxBleConnection: RxBleConnection? = null

    fun enabledObserveBleState(enabled: Boolean): Boolean {
        val success = if (isObserveBleStateEnabled != enabled) {
            if (enabled)
                observeBleState()
            else
                disposeObserveBleState()
            true
        } else {
            false
        }

        isObserveBleStateEnabled = enabled
        return success
    }

    private fun observeBleState() {
        Timber.d("observeBleState")
        rxBleClient
            .observeStateChanges()
            .observeOn(AndroidSchedulers.mainThread())
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
                disposeObserveBleState()
            })
            .apply {
                bleStateSubscriptionDisposable = this
            }

        isObserveBleStateEnabled = true
    }

    private fun disposeObserveBleState() {
        Timber.d("disposeObserveBleState")
        if (bleStateSubscriptionDisposable != null) {
            bleStateSubscriptionDisposable?.dispose()
            bleStateSubscriptionDisposable = null
            isObserveBleStateEnabled = false
        }
    }

    fun enabledScan(enabled: Boolean): Boolean {
        var success: Boolean = if (isScanEnabled != enabled) {

            if (autoCloseScanSubscriptionDisposable != null) {
                autoCloseScanSubscriptionDisposable?.dispose()
                autoCloseScanSubscriptionDisposable = null
            }

            if (enabled) {
                Single
                    .create<() -> Unit>
                    {
                        scan()
                        it.onSuccess(::disposeScan)
                    }
                    .delay(AUTO_CLOSE_SCAN_TIME, TimeUnit.SECONDS)
                    .subscribe { disposeScan ->
                        disposeScan.invoke()
                    }
                    .apply {
                        autoCloseScanSubscriptionDisposable = this
                    }

            } else
                disposeScan()
            true
        } else {
            false
        }

        isScanEnabled = enabled
        return success
    }

    private fun scan() {
        Timber.d("scan")
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
                        scanResult.bleDevice,
                        scanResult.bleDevice.macAddress,
                        scanResult.bleDevice.name!!,
                        scanResult.rssi
                    )
                    Timber.d(scanResult.toString())
                }
            }, { throwable ->
                Timber.d(throwable)
                bleListener?.onScanError(throwable.toString())
                disposeScan()
            })
            .apply {
                scanSubscriptionDisposable = this
            }
        isScanEnabled = true
    }

    private fun disposeScan() {
        Timber.d("disposeScan")
        if (scanSubscriptionDisposable != null) {
            scanSubscriptionDisposable?.dispose()
            scanSubscriptionDisposable = null
            isScanEnabled = false
        }
    }

    fun enabledConnectBle(enabled: Boolean, device: BluetoothDevice?): Boolean {
        if (!enabled) {
            disposeConnectBle()
        } else {
            if (device?.address?.isEmpty()!!)
                return isConnectBleEnabled

            this.device = device
            if (isConnectBleEnabled != enabled && enabled)
                connectBle(device.address)
        }
        isConnectBleEnabled = enabled
        return isConnectBleEnabled
    }

    private fun connectBle(macAddress: String) {
        if (connectBleSubscriptionDisposable != null)
            return

        Timber.d("connectBle")
        val rxBleDevice = rxBleClient.getBleDevice(macAddress)
        rxBleDevice
            .establishConnection(false)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ rxBleConnection ->
                this.rxBleConnection = rxBleConnection
                Timber.d("rxBleConnection.mtu:${rxBleConnection.mtu}")

                bleListener?.onBleConnected(macAddress)

                stopConnectBleTimer()
            }, { throwable ->
                Timber.d(throwable)
                bleListener?.onConnectBleError(throwable.toString())
                disposeConnectBle()
            })
            .apply {
                connectBleSubscriptionDisposable = this
            }

        isConnectBleEnabled = true
    }

    private fun disposeConnectBle() {
        Timber.d("disposeConnectBle")
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
        Timber.d("discoverService")
        rxBleConnection
            .discoverServices()

            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ services ->
                services
                    .bluetoothGattServices
                    .forEach { bluetoothGattService ->
                        val characteristicUuids = bluetoothGattService
                            .characteristics
                            .map {
                                it.uuid
                            }
                        bleListener?.onDiscoverBleService(
                            bluetoothGattService.uuid,
                            characteristicUuids
                        )
                    }
            }, { throwable ->
                Timber.d(throwable)
                bleListener?.onDiscoverBleServiceError(throwable.toString())
                disposeDiscoverService()
            })
            .apply {
                discoverServiceSubscriptionDisposable = this
            }
        isDiscoverServiceEnabled = true
    }

    private fun disposeDiscoverService() {
        Timber.d("disposeDiscoverService")
        if (discoverServiceSubscriptionDisposable != null) {
            discoverServiceSubscriptionDisposable?.dispose()
            discoverServiceSubscriptionDisposable = null
            isDiscoverServiceEnabled = false
        }
    }

    fun enabledReadBleCharacteristicValue(enabled: Boolean, characteristicUuid: UUID): Boolean {
        if (rxBleConnection == null)
            return isReadCharacteristicEnabled

        if (isReadCharacteristicEnabled != enabled) {
            if (enabled)
                readBleCharacteristicValue(rxBleConnection!!, characteristicUuid)
            else
                disposeReadBleCharacteristicValue()
        }

        isReadCharacteristicEnabled = enabled
        return isReadCharacteristicEnabled
    }

    private fun readBleCharacteristicValue(
        rxBleConnection: RxBleConnection,
        characteristicUuid: UUID
    ) {
        Timber.d("readBleCharacteristicValue")
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
                    disposeReadBleCharacteristicValue()
                }
            )
            .apply {
                readCharacteristicValueSubscriptionDisposable = this
            }
        isReadCharacteristicEnabled = true
    }

    private fun disposeReadBleCharacteristicValue() {
        Timber.d("disposeReadBleCharacteristicValue")
        if (readCharacteristicValueSubscriptionDisposable != null) {
            readCharacteristicValueSubscriptionDisposable?.dispose()
            readCharacteristicValueSubscriptionDisposable = null
            isReadCharacteristicEnabled = false
        }
    }

    fun enabledListenNotification(enabled: Boolean, characteristicUuid: UUID): Boolean {
        if (rxBleConnection == null)
            return isListenNotificationEnabled

        if (isListenNotificationEnabled != enabled) {
            if (enabled)
                listenNotification(rxBleConnection!!, characteristicUuid)
            else
                disposeListenNotification()
        }

        isListenNotificationEnabled = enabled
        return isListenNotificationEnabled
    }

    private fun listenNotification(rxBleConnection: RxBleConnection, characteristicUuid: UUID) {
        Timber.d("listenNotification")
        rxBleConnection
            .setupNotification(characteristicUuid)
            .flatMap {
                it
            }
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ bytes ->
                Timber.d("${bytes[1].toInt()}")
                bleListener?.onListenNotification(characteristicUuid, bytes[1].toInt())
            }, { throwable ->
                Timber.d(throwable)
                bleListener?.onListenNotificationError(throwable.toString())
                disposeListenNotification()
            })
            .apply {
                listenNotificationDisposable = this
            }
        isListenNotificationEnabled = true
    }

    private fun disposeListenNotification() {
        Timber.d("disposeListenNotification")
        if (listenNotificationDisposable != null) {
            listenNotificationDisposable?.dispose()
            listenNotificationDisposable = null
            isListenNotificationEnabled = false
        }
    }

    fun getBleDevice(macAddress: String): RxBleDevice? {
        if (macAddress.isEmpty())
            return null
        return rxBleClient.getBleDevice(macAddress)
    }

    fun getBluetoothDevice(macAddress: String): BluetoothDevice? {
        return rxBleClient.getBleDevice(macAddress).bluetoothDevice
    }

    fun startConnectBleTimer() {
        if (connectBleTimerDisposable != null)
            return

        Single
            .create<Boolean> {
                it.onSuccess(isConnectBleEnabled)
            }
            .repeatWhen { completed ->
                completed.delay(2, TimeUnit.SECONDS)
            }
            .subscribe { isConnectBleEnabled ->
                Timber.d("connect ble enabled:$isConnectBleEnabled")
                if (!isConnectBleEnabled) {
                    if (device?.address != null)
                        connectBle(device!!.address)
                }
            }
            .apply {
                connectBleTimerDisposable = this
            }
    }

    private var connectBleTimerDisposable: Disposable? = null
    fun stopConnectBleTimer() {
        if (connectBleTimerDisposable != null) {
            connectBleTimerDisposable?.dispose()
            connectBleTimerDisposable = null
        }
    }


    interface IBleListener {
        fun onObserveBleState(state: String)
        fun onObserveBleStateError(error: String)

        fun onScan(rxBleDevice: RxBleDevice, macAddress: String, deviceName: String, rssi: Int)
        fun onScanError(error: String)

        fun onBleConnected(macAddress: String)
        fun onConnectBleError(error: String)

        fun onDiscoverBleService(gattServiceUuid: UUID, characteristicUuids: List<UUID>)

        fun onDiscoverBleService(serviceUuid: UUID)
        fun onDiscoverBleServiceError(error: String)

        fun onReadBleCharacteristicValue(value: String)
        fun onReadBleCharacteristicValueError(error: String)

        fun onListenNotification(characteristicUuid: UUID, value: Int)
        fun onListenNotificationError(error: String)
    }
}