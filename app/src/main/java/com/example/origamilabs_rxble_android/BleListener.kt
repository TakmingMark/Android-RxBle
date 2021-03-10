package com.example.origamilabs_rxble_android

import java.util.*

open class BleListener :BleHelper.IBleListener{
    override fun onObserveBleState(state: String) {
    }

    override fun onObserveBleStateError(error: String) {
    }

    override fun onScan(macAddress: String, deviceName: String, rssi: Int) {
    }

    override fun onScanError(error: String) {
    }

    override fun onConnectBle(macAddress: String) {
    }

    override fun onConnectBleError(error: String) {
    }

    override fun onDiscoverBleService(serviceUuid: UUID) {
    }

    override fun onDiscoverBleServiceError(error: String) {
    }

    override fun onReadBleCharacteristicValue(value: String) {
    }

    override fun onReadBleCharacteristicValueError(error: String) {
    }

    override fun onListenNotification(characteristicUuid: UUID, value: Int) {
    }

    override fun onListenNotificationError(error: String) {
    }
}