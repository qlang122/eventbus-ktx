package com.qlang.eventbus

import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.coroutines.CoroutineContext

/**
 * ktx版 Eventbus
 *
 * 原作：https://github.com/fengzhizi715/EventBus
 *
 */
object EventBus : CoroutineScope {
    @JvmField
    val MAIN: CoroutineDispatcher = Dispatchers.Main

    @JvmField
    val DEFAULT: CoroutineDispatcher = Dispatchers.Default

    @JvmField
    val IO: CoroutineDispatcher = Dispatchers.IO

    private val TAG = "EventBus"

    private val job = SupervisorJob()

    override val coroutineContext: CoroutineContext = Dispatchers.Default + job

    private val contextMap = ConcurrentHashMap<String, MutableMap<Class<*>, EventData<*>>>()
    private val mStickyEventMap = ConcurrentHashMap<Class<*>, Any>()

    @JvmStatic
    fun <T> register(
            contextName: String,
            eventClass: Class<T>,
            onEvent: (T) -> Unit,
            eventDispatcher: CoroutineDispatcher = MAIN,
            onException: ((Throwable) -> Unit)? = null
    ) {
        val eventDataMap = if (contextMap.containsKey(contextName)) {
            contextMap[contextName]!!
        } else {
            val eventDataMap = mutableMapOf<Class<*>, EventData<*>>()
            contextMap[contextName] = eventDataMap
            eventDataMap
        }

        eventDataMap[eventClass] = EventData(this, eventDispatcher, onEvent, onException)
    }

    @JvmStatic
    fun <T> registerSticky(
            contextName: String,
            eventClass: Class<T>,
            onEvent: (T) -> Unit,
            eventDispatcher: CoroutineDispatcher = MAIN,
            onException: ((Throwable) -> Unit)? = null
    ) {
        val eventDataMap = if (contextMap.containsKey(contextName)) {
            contextMap[contextName]!!
        } else {
            val eventDataMap = mutableMapOf<Class<*>, EventData<*>>()
            contextMap[contextName] = eventDataMap
            eventDataMap
        }

        eventDataMap[eventClass] = EventData(this, eventDispatcher, onEvent, onException)

        mStickyEventMap[eventClass]?.let { postEvent(it) }
    }

    @JvmStatic
    fun post(event: Any, delayTime: Long = 0) {
        if (delayTime > 0) {
            launch {
                delay(delayTime)
                postEvent(event)
            }
        } else {
            postEvent(event)
        }
    }

    @JvmStatic
    fun postSticky(event: Any) {
        mStickyEventMap[event.javaClass] = event
    }

    @JvmStatic
    fun unregisterAll() {
        coroutineContext.cancelChildren()
        for ((_, eventDataMap) in contextMap) {
            eventDataMap.values.forEach {
                it.cancel()
            }
            eventDataMap.clear()
        }
        contextMap.clear()
    }

    @JvmStatic
    fun unregister(contextName: String) {
        val cloneContexMap = ConcurrentHashMap<String, MutableMap<Class<*>, EventData<*>>>()
        cloneContexMap.putAll(contextMap)
        val map = cloneContexMap.filter { it.key == contextName }
        for ((_, eventDataMap) in map) {
            eventDataMap.values.forEach {
                it.cancel()
            }
            eventDataMap.clear()
        }
        contextMap.remove(contextName)
    }

    @JvmStatic
    fun <T> removeStickyEvent(eventType: Class<T>) {
        mStickyEventMap.remove(eventType)
    }

    private fun postEvent(event: Any) {
        val cloneContexMap = ConcurrentHashMap<String, MutableMap<Class<*>, EventData<*>>>()
        cloneContexMap.putAll(contextMap)
        for ((_, eventDataMap) in cloneContexMap) {
            eventDataMap.keys
                    .firstOrNull { it == event.javaClass || it == event.javaClass.superclass }
                    ?.let { key -> eventDataMap[key]?.postEvent(event) }
        }
    }

}
