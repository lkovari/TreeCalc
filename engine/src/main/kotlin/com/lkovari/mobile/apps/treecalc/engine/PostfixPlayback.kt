package com.lkovari.mobile.apps.treecalc.engine

data class PostfixPlayback(
    val index: Int? = null,
    val running: Boolean = false
) {
    fun toggle(count: Int, stepByStep: Boolean): PostfixPlayback {
        if (count <= 0) {
            return this
        }
        if (running) {
            return copy(running = false)
        }
        if (stepByStep) {
            return step(count)
        }
        if (index == null || index >= count - 1) {
            return copy(index = 0, running = true)
        }
        return copy(running = true)
    }

    fun tick(count: Int): PostfixPlayback {
        if (!running || count <= 0) {
            return this
        }
        if (index != null && index + 1 < count) {
            return copy(index = index + 1)
        }
        return copy(running = false)
    }

    fun select(index: Int): PostfixPlayback {
        if (this.index == index) {
            return copy(index = null, running = false)
        }
        return copy(index = index, running = false)
    }

    fun step(count: Int): PostfixPlayback {
        if (count <= 0) {
            return this
        }
        val current = index
        if (current == null) {
            return copy(index = 0, running = false)
        }
        if (current + 1 < count) {
            return copy(index = current + 1, running = false)
        }
        return copy(running = false)
    }

    fun stop(): PostfixPlayback {
        return PostfixPlayback()
    }
}
