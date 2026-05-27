package com.example.filamenthmi.scene

import com.google.protobuf.CodedOutputStream
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ByteArrayOutputStream

class DrivingSceneProtoParserTest {
    @Test
    fun parseMinimalFrame() {
        val frame = DrivingSceneProtoParser.parse(buildFrameBytes())
        assertEquals(1000L, frame.timestampMs)
        assertEquals(1.5f, frame.egoPose.z)
        assertEquals(1, frame.vehicles.size)
        assertEquals(19.9f, frame.hud.speedKmh)
    }
    private fun buildFrameBytes(): ByteArray { val o=ByteArrayOutputStream(); val c=CodedOutputStream.newInstance(o); c.writeUInt64(2,1000L); c.writeTag(3,2); c.writeByteArrayNoTag(buildPose(0f,0f,1.5f,0f,0f,8f)); c.writeTag(4,2); c.writeByteArrayNoTag(buildVehicle()); c.writeTag(6,2); c.writeByteArrayNoTag(buildHud()); c.flush(); return o.toByteArray() }
    private fun buildVehicle(): ByteArray { val o=ByteArrayOutputStream(); val c=CodedOutputStream.newInstance(o); c.writeUInt64(1,7); c.writeTag(2,2); c.writeByteArrayNoTag(buildPose(0f,0f,5f,0f,0f,0f)); c.flush(); return o.toByteArray() }
    private fun buildHud(): ByteArray { val o=ByteArrayOutputStream(); val c=CodedOutputStream.newInstance(o); c.writeString(1,"D"); c.writeFloat(2,19.9f); c.writeBool(3,true); c.writeUInt32(4,130); c.flush(); return o.toByteArray() }
    private fun buildPose(x: Float,y: Float,z: Float,vx: Float,vy: Float,vz: Float): ByteArray { val o=ByteArrayOutputStream(); val c=CodedOutputStream.newInstance(o); c.writeTag(1,2); c.writeByteArrayNoTag(buildVec3(x,y,z)); c.writeTag(3,2); c.writeByteArrayNoTag(buildVec3(vx,vy,vz)); c.flush(); return o.toByteArray() }
    private fun buildVec3(x: Float,y: Float,z: Float): ByteArray { val o=ByteArrayOutputStream(); val c=CodedOutputStream.newInstance(o); c.writeFloat(1,x); c.writeFloat(2,y); c.writeFloat(3,z); c.flush(); return o.toByteArray() }
}
