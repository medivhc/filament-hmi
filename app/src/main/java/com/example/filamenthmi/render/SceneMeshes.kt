package com.example.filamenthmi.render

import kotlin.math.sqrt

data class MeshData(
    val positions: FloatArray,
    val indices: ShortArray
)

object RoadMeshBuilder {
    fun build(lengthM: Float = 120f, widthM: Float = 8f): MeshData {
        val l = lengthM / 2f
        val w = widthM / 2f
        val positions = floatArrayOf(
            -w, 0f, -l,
            w, 0f, -l,
            w, 0f, l,
            -w, 0f, l
        )
        val indices = shortArrayOf(0, 1, 2, 0, 2, 3)
        return MeshData(positions, indices)
    }
}

object LaneGuideMeshBuilder {
    fun buildStrip(centerline: List<InterpolationPose>, widthM: Float = 1.2f): MeshData {
        require(centerline.size >= 2) { "centerline needs at least 2 points" }
        val half = widthM / 2f
        val positions = ArrayList<Float>(centerline.size * 2 * 3)
        val indices = ArrayList<Short>((centerline.size - 1) * 6)

        for (i in centerline.indices) {
            val p = centerline[i]
            val dir = when {
                i == centerline.lastIndex -> normalize(centerline[i].x - centerline[i - 1].x, centerline[i].z - centerline[i - 1].z)
                else -> normalize(centerline[i + 1].x - centerline[i].x, centerline[i + 1].z - centerline[i].z)
            }
            val nx = -dir.second
            val nz = dir.first

            positions += (p.x + nx * half)
            positions += p.y
            positions += (p.z + nz * half)

            positions += (p.x - nx * half)
            positions += p.y
            positions += (p.z - nz * half)

            if (i < centerline.lastIndex) {
                val base = (i * 2).toShort()
                indices += base
                indices += (base + 1).toShort()
                indices += (base + 2).toShort()
                indices += (base + 1).toShort()
                indices += (base + 3).toShort()
                indices += (base + 2).toShort()
            }
        }

        return MeshData(positions.toFloatArray(), indices.toShortArray())
    }

    private fun normalize(x: Float, z: Float): Pair<Float, Float> {
        val len = sqrt(x * x + z * z).coerceAtLeast(1e-5f)
        return Pair(x / len, z / len)
    }
}
