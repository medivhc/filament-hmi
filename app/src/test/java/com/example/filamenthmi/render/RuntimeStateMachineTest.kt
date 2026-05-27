package com.example.filamenthmi.render

import org.junit.Assert.assertEquals
import org.junit.Test

class RuntimeStateMachineTest {
    @Test
    fun transitionsFollowLifecycleOrder() {
        val sm = RuntimeStateMachine()
        sm.onResume()
        sm.onPause()
        sm.onResume()
        sm.onDestroy()
        sm.onResume()
        assertEquals(RuntimeStateMachine.State.DESTROYED, sm.state)
    }
}
