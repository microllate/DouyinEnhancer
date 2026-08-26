package io.github.twyora.douyinenhancer.utils

import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.extension.toClass as KavaRefExt_toClass
import com.highcapable.kavaref.resolver.FieldResolver
import com.highcapable.kavaref.resolver.MethodResolver
import com.highcapable.yukihookapi.hook.log.YLog

data class Field(val name: String?)

data class Method(val name: String?, val parameters: List<String>?)

@JvmOverloads
fun String.toClass(loader: ClassLoader? = null, initialize: Boolean = false): Class<*> {
    // KavaRef's toClass doesn't support primitive types and JVM descriptors, manually handle them here
    return when (this) {
        "I", "int" -> Int::class.javaPrimitiveType!!
        "Z", "boolean" -> Boolean::class.javaPrimitiveType!!
        "B", "byte" -> Byte::class.javaPrimitiveType!!
        "C", "char" -> Char::class.javaPrimitiveType!!
        "S", "short" -> Short::class.javaPrimitiveType!!
        "J", "long" -> Long::class.javaPrimitiveType!!
        "F", "float" -> Float::class.javaPrimitiveType!!
        "D", "double" -> Double::class.javaPrimitiveType!!
        "V", "void" -> Void.TYPE
        else -> {
            if (this.startsWith("L") && this.endsWith(";")) {
                this.substring(1, this.length - 1).replace("/", ".").KavaRefExt_toClass(loader, initialize)
            } else {
                this.KavaRefExt_toClass(loader, initialize)
            }
        }
    }
}

@JvmOverloads
fun String.toClassOrNull(loader: ClassLoader? = null, initialize: Boolean = false) = runCatching {
    this.toClass(loader, initialize)
}.getOrNull()

fun Iterable<String>.toClasses(): Array<Class<*>> {
    return this.map {
        it.toClass()
    }.toTypedArray()
}

fun Array<String>.toClasses(): Array<Class<*>> = Array(size) {
    get(it).toClass()
}

fun <T : Any> Class<T>.resolveMethod(method: Method): MethodResolver<T>? {
    if (method.name.isNullOrBlank()) {
        YLog.error("cannot determine which method to resolve on ${this.name}, name is null or blank")
        return null
    }
    return runCatching {
        this.resolve().firstMethodOrNull {
            name = method.name
            method.parameters?.let {
                parameters(*it.toClasses())
            }
            superclass()
        }
    }.onFailure { throwable ->
        YLog.error("resolve failed: ${this.name}.${method.name}(${method.parameters})", throwable)
    }.getOrNull().also {
        if (it == null) {
            YLog.error("method not found: ${this.name}.${method.name}(${method.parameters})")
        }
    }
}

fun <T : Any> Class<T>.resolveField(field: Field): FieldResolver<T>? {
    if (field.name.isNullOrBlank()) {
        YLog.error("cannot determine which field to resolve on ${this.name}, name is null or blank")
        return null
    }
    return runCatching {
        this.resolve().firstFieldOrNull {
            name = field.name
            superclass()
        }
    }.onFailure { throwable ->
        YLog.error("resolve failed: ${this.name}.${field.name}", throwable)
    }.getOrNull().also {
        if (it == null) {
            YLog.error("field not found: ${this.name}.${field.name}")
        }
    }
}

fun <T : Any> T.resolveMethod(method: Method): MethodResolver<T>? {
    @Suppress("UNCHECKED_CAST")
    val thisClass = this::class.java as Class<T>
    return thisClass.resolveMethod(method)?.of(this)
}

fun <T : Any> T.resolveField(field: Field): FieldResolver<T>? {
    @Suppress("UNCHECKED_CAST")
    val thisClass = this::class.java as Class<T>
    return thisClass.resolveField(field)?.of(this)
}

inline fun <reified T> Class<*>.invokeStaticMethod(method: Method, vararg args: Any?): T? = this.resolveMethod(method)?.invoke(*args) as? T

inline fun <reified T> Any.invokeMethod(method: Method, vararg args: Any?): T? = this.resolveMethod(method)?.invoke(*args) as? T

fun Any.invokeMethodOnly(method: Method, vararg args: Any?) {
    this.invokeMethod<Any>(method, *args)
}

inline fun <reified T> Class<*>.getStaticField(field: Field): T? = this.resolveField(field)?.get() as? T

inline fun <reified T> Any.getField(field: Field): T? = this.resolveField(field)?.get() as? T

fun Class<*>.setStaticField(field: Field, value: Any?) = this.resolveField(field)?.set(value)

fun Any.setField(field: Field, value: Any?) = this.resolveField(field)?.set(value)
