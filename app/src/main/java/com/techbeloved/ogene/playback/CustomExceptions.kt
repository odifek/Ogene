package com.techbeloved.ogene.playback

data class InvalidMediaIdException(override val message: String): Throwable(message)

data class ServiceNotReadyException(override val message: String): Throwable(message)

data class QueueManagerNotReadyException(override val message: String): Throwable(message)

data class EndOfQueueException(override val message: String): Throwable(message)
