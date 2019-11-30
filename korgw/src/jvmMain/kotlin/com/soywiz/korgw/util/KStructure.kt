package com.soywiz.korgw.util

import com.sun.jna.*
import kotlin.reflect.KProperty

open class KStructure(pointer: Pointer? = null) : NativeMapped {
    protected val layout = LayoutBuilder()
    val size get() = layout.size

    private var _pointer: Pointer? = pointer; private set
    val pointer: Pointer by lazy {
        if (_pointer == null) _pointer = Memory(size.toLong())
        _pointer!!
    }

    fun byte() = layout.byte()
    fun int() = layout.integer()
    fun nativeLong() = layout.nativeLong()
    fun <T> pointer() = layout.pointer<T>()

    override fun nativeType(): Class<*> = Pointer::class.java
    override fun toNative(): Any = this.pointer
    override fun fromNative(nativeValue: Any?, context: FromNativeContext?): Any = this::class.constructors.first().call(nativeValue)
}

class LayoutBuilder {
    private var offset = 0
    var maxAlign = 4

    fun align(size: Int) = this.apply {
        maxAlign = kotlin.math.max(maxAlign, size)
        while (this.offset % size != 0) this.offset++
    }

    val size by lazy {
        align(maxAlign)
        offset
    }

    private fun alloc(size: Int) = align(size).offset.also { this.offset += size }

    //fun int() = alloc(Int.SIZE_BYTES)
    //fun nativeLong() = alloc(NativeLong.SIZE)

    fun byte() = DelegateByteProperty(alloc(Byte.SIZE_BYTES))
    fun integer() = DelegateIntProperty(alloc(Int.SIZE_BYTES))
    fun nativeLong() = DelegateNativeLongProperty(alloc(NativeLong.SIZE))
    fun <T> pointer() = DelegatePointerProperty<T>(alloc(Native.POINTER_SIZE))
}

inline class DelegateByteProperty(val offset: Int) {
    operator fun getValue(obj: KStructure, property: KProperty<*>): Byte = obj.pointer.getByte(offset.toLong())
    operator fun setValue(obj: KStructure, property: KProperty<*>, i: Byte) = run { obj.pointer.setByte(offset.toLong(), i) }
}

inline class DelegateIntProperty(val offset: Int) {
    operator fun getValue(obj: KStructure, property: KProperty<*>): Int = obj.pointer.getInt(offset.toLong())
    operator fun setValue(obj: KStructure, property: KProperty<*>, i: Int) = run { obj.pointer.setInt(offset.toLong(), i) }
}

inline class DelegateNativeLongProperty(val offset: Int) {
    operator fun getValue(obj: KStructure, property: KProperty<*>): NativeLong = obj.pointer.getNativeLong(offset.toLong())
    operator fun setValue(obj: KStructure, property: KProperty<*>, i: NativeLong) = run { obj.pointer.setNativeLong(offset.toLong(), i) }
}

inline class DelegatePointerProperty<T>(val offset: Int) {
    operator fun getValue(obj: KStructure, property: KProperty<*>): T = TODO()
    operator fun setValue(obj: KStructure, property: KProperty<*>, i: T): Unit = TODO()
}
