package com.example.filamenthmi.render

class RuntimeStateMachine {
    enum class State { IDLE, RUNNING, PAUSED, DESTROYED }

    var state: State = State.IDLE
        private set

    fun onResume() {
        if (state != State.DESTROYED) state = State.RUNNING
    }

    fun onPause() {
        if (state == State.RUNNING) state = State.PAUSED
    }

    fun onDestroy() {
        state = State.DESTROYED
    }
}
