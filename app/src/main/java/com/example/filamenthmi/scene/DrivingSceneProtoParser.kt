package com.example.filamenthmi.scene

import com.google.protobuf.CodedInputStream

object DrivingSceneProtoParser {
    fun parse(bytes: ByteArray): DrivingSceneFrameModel {
        val input = CodedInputStream.newInstance(bytes)
        var timestampMs = 0L
        var egoPose = Pose3()
        val vehicles = mutableListOf<VehicleState>()
        var hud = HudState()

        while (!input.isAtEnd) {
            when (input.readTag()) {
                0 -> break
                16 -> timestampMs = input.readUInt64()
                26 -> egoPose = readPose(readSub(input))
                34 -> vehicles += readVehicle(readSub(input))
                50 -> hud = readHud(readSub(input))
                else -> input.skipField(input.lastTag)
            }
        }
        return DrivingSceneFrameModel(timestampMs, egoPose, vehicles, hud)
    }

    private fun readVehicle(input: CodedInputStream): VehicleState {
        var id = 0L
        var pose = Pose3()
        var length = 4.5f
        var width = 1.8f
        var height = 1.6f
        var isEgo = false
        while (!input.isAtEnd) {
            when (input.readTag()) {
                0 -> break
                8 -> id = input.readUInt64()
                18 -> pose = readPose(readSub(input))
                29 -> length = input.readFloat()
                37 -> width = input.readFloat()
                45 -> height = input.readFloat()
                48 -> isEgo = input.readBool()
                else -> input.skipField(input.lastTag)
            }
        }
        return VehicleState(id, pose, length, width, height, isEgo)
    }

    private fun readPose(input: CodedInputStream): Pose3 {
        var x = 0f; var y = 0f; var z = 0f
        var vx = 0f; var vy = 0f; var vz = 0f
        while (!input.isAtEnd) {
            when (input.readTag()) {
                0 -> break
                10 -> { val v = readVec3(readSub(input)); x=v[0]; y=v[1]; z=v[2] }
                26 -> { val v = readVec3(readSub(input)); vx=v[0]; vy=v[1]; vz=v[2] }
                else -> input.skipField(input.lastTag)
            }
        }
        return Pose3(x,y,z,vx,vy,vz)
    }

    private fun readVec3(input: CodedInputStream): FloatArray {
        var x=0f; var y=0f; var z=0f
        while (!input.isAtEnd) {
            when (input.readTag()) {
                0 -> break
                13 -> x = input.readFloat()
                21 -> y = input.readFloat()
                29 -> z = input.readFloat()
                else -> input.skipField(input.lastTag)
            }
        }
        return floatArrayOf(x,y,z)
    }

    private fun readHud(input: CodedInputStream): HudState {
        var speed = 0f
        var gear = "D"
        var acc = true
        var limit = 130
        while (!input.isAtEnd) {
            when (input.readTag()) {
                0 -> break
                10 -> gear = input.readString()
                21 -> speed = input.readFloat()
                24 -> acc = input.readBool()
                32 -> limit = input.readUInt32()
                else -> input.skipField(input.lastTag)
            }
        }
        return HudState(speed, gear, acc, limit)
    }

    private fun readSub(input: CodedInputStream): CodedInputStream {
        val size = input.readRawVarint32()
        val bytes = input.readRawBytes(size)
        return CodedInputStream.newInstance(bytes)
    }
}
