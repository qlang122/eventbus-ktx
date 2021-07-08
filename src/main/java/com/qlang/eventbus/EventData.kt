package com.qlang.eventbus

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.launch

data class EventData<E>(
        val coroutineScope: CoroutineScope,
        val eventDispatcher: CoroutineDispatcher,
        val onEvent: (E) -> Unit,
        val onException: ((Throwable) -> Unit)? = null) {

    private val channel = Channel<E>()

    init {
        coroutineScope.launch {
            channel.consumeEach {
                // 消费者循环地消费消息
                launch(eventDispatcher) {
                    try {
                        onEvent(it)
                    } catch (e: Throwable) {
                        e.printStackTrace()
                        onException?.invoke(e)
                    }
                }
            }
        }
    }

    fun postEvent(event: Any) {
        if (!channel.isClosedForSend) {
            coroutineScope.launch {
                channel.send(event as E)
            }
        } else {
            System.out.println("[EventData]: Channel is closed for send")
        }
    }

    fun cancel() {
        channel.cancel()
    }
}