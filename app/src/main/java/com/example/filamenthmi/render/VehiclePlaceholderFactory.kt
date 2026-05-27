package com.example.filamenthmi.render

/**
 * Creates simple white-model placeholder dimensions for ego/traffic vehicles.
 */
object VehiclePlaceholderFactory {
    fun boxMesh(lengthM: Float = 4.6f, widthM: Float = 1.9f, heightM: Float = 1.7f): MeshData {
        val lx = widthM / 2f
        val ly = heightM
        val lz = lengthM / 2f
        val positions = floatArrayOf(
            -lx, 0f, -lz,  lx, 0f, -lz,  lx, 0f, lz,  -lx, 0f, lz,
            -lx, ly, -lz,  lx, ly, -lz,  lx, ly, lz,  -lx, ly, lz
        )
        val indices = shortArrayOf(
            0,1,2, 0,2,3,
            4,5,6, 4,6,7,
            0,1,5, 0,5,4,
            1,2,6, 1,6,5,
            2,3,7, 2,7,6,
            3,0,4, 3,4,7
        )
        return MeshData(positions, indices)
    }
}
