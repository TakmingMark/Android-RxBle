package com.example.origamilabs_rxble_android

import io.reactivex.Observable
import org.junit.Assert.assertEquals
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*
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
        val scanObservable =
            Observable.just(
                "Hello",
                " World!",
                " This",
                " is",
                " Wonderful",
                " World"
            )
                .scan { t1: String, t2: String ->

                    println(t1)
                    t1 + t2
                }


        scanObservable.subscribe {
//            println(it)
        }
    }

    @Test
    fun test(){
    }
    @Test
    fun concatMap() {

        Observable.just("A", "B", "C")
            .concatMap { i ->

                Observable.intervalRange(0, 3, 0, 1, TimeUnit.SECONDS)
                    .map { n->
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