package com.example.origamilabs_rxble_android

import io.reactivex.Completable
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.disposables.Disposable
import org.junit.Assert.assertEquals
import org.junit.Test
import timber.log.Timber
import java.util.concurrent.TimeUnit

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }


    @Test
    fun scan() {
//        val myObservable = Observable.just("a", "b", "c")
//        myObservable.repeatWhen { completed ->
//            completed.delay(
//                3,
//                TimeUnit.SECONDS
//            )
//        }
//            .subscribe { x: String? -> println(x) }

        var time = 0
        var disposable:Disposable?=null
        Single.create<Boolean> {
            time++
            it.onSuccess(true)

        }.repeatWhen { completed ->
            completed.delay(3, TimeUnit.SECONDS)
        }
            .subscribe {
                println(it)
                if(time==3)
                    disposable?.dispose()
            }
            .apply {
                disposable=this
            }
        Thread.sleep(15000)
//        Completable
//            .create {
//                it.onComplete()
//            }
//            .repeatWhen{completed->
//                completed.delay (3,TimeUnit.SECONDS)
//            }
//            .subscribe {
//
//            }
    }

    @Test
    fun test() {
    }

    @Test
    fun concatMap() {

        Observable.just("A", "B", "C")
            .concatMap { i ->

                Observable.intervalRange(0, 3, 0, 1, TimeUnit.SECONDS)
                    .map { n ->
                        println(n)
                        "($n : $i)"
                    }
            }.subscribe {

                println(it)
            }
//        Thread.sleep(5000)
        //(0 : A)
        //(1 : A)
        //(2 : A)
        //(0 : B)
        //(1 : B)
        //(2 : B)
        //(0 : C)
        //(1 : C)
    }
}