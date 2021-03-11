package com.example.origamilabs_rxble_android

import android.bluetooth.BluetoothGatt
import com.polidea.rxandroidble2.RxBleCustomOperation
import com.polidea.rxandroidble2.RxBleRadioOperationCustom
import com.polidea.rxandroidble2.internal.connection.RxBleGattCallback
import io.reactivex.Observable
import io.reactivex.Scheduler
import io.reactivex.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit

class CustomRefresh: RxBleCustomOperation<Boolean> {

    @Throws(Throwable::class)
    override fun asObservable(bluetoothGatt: BluetoothGatt,
                              rxBleGattCallback: RxBleGattCallback,
                              scheduler: Scheduler
    ): Observable<Boolean> {

        return Observable.fromCallable {
            refreshDeviceCache(bluetoothGatt) }
            .delay(500, TimeUnit.MILLISECONDS, Schedulers.computation())
            .subscribeOn(scheduler)
    }

    private fun refreshDeviceCache(gatt: BluetoothGatt): Boolean {
        var isRefreshed = false

        try {
            val localMethod = gatt.javaClass.getMethod("refresh")
            if (localMethod != null) {
                isRefreshed = (localMethod.invoke(gatt) as Boolean)
                Timber.i("Gatt cache refresh successful: [%b]", isRefreshed)
            }
        } catch (localException: Exception) {
            Timber.e("An exception occured while refreshing device" + localException.toString())
        }

        return isRefreshed
    }
}