package net.typho.entityOutlines

import org.joml.Vector3f

data class StoredQuad(
    @JvmField
    val v0: Vector3f,
    @JvmField
    val v1: Vector3f,
    @JvmField
    val v2: Vector3f,
    @JvmField
    val v3: Vector3f,
    @JvmField
    val n0: Vector3f,
    @JvmField
    val n1: Vector3f,
    @JvmField
    val n2: Vector3f,
    @JvmField
    val n3: Vector3f
)
