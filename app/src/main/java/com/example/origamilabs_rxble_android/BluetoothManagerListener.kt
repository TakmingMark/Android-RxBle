package com.example.origamilabs_rxble_android

import java.util.*

open class BluetoothManagerListener : BluetoothManager.IBluetoothManagerListener {
    override fun onBondState(state: Int) {

    }

    override fun onA2dpState(state: Int) {

    }

    override fun onObserveBleState(state: String) {

    }

    override fun onObserveBleStateError(error: String) {

    }

    override fun onScan(macAddress: String, deviceName: String, rssi: Int) {

    }

    override fun onScanError(error: String) {

    }

    override fun onBleConnected(macAddress: String) {

    }

    override fun onConnectBleError(error: String) {

    }

    override fun onDiscoverBleService(serviceUuid: UUID) {
        TODO("Not yet implemented")
    }

    override fun onDiscoverBleServiceError(error: String) {
        TODO("Not yet implemented")
    }

    override fun onReadBleCharacteristicValue(value: String) {
        TODO("Not yet implemented")
    }

    override fun onReadBleCharacteristicValueError(error: String) {
        TODO("Not yet implemented")
    }

    override fun onListenNotification(characteristicUuid: UUID, value: Int) {
        TODO("Not yet implemented")
    }

    override fun onListenNotificationError(error: String) {
        TODO("Not yet implemented")
    }
}