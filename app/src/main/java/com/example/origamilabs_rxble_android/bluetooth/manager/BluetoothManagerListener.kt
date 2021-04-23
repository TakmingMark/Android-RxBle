package com.example.origamilabs_rxble_android.bluetooth.manager

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

    }

    override fun onDiscoverBleService(gattServiceUuid: UUID, characteristicUuids: List<UUID>) {

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

    override fun onAutoConnectSuccess() {

    }

    override fun onAutoConnectError(error: String) {

    }
}