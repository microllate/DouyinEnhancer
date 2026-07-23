package io.github.twyora.douyinenhancer.utils

import com.highcapable.kavaref.KavaRef.Companion.asResolver
import com.highcapable.kavaref.KavaRef.Companion.resolve
import com.highcapable.kavaref.resolver.FieldResolver
import com.highcapable.kavaref.resolver.MethodResolver
import com.highcapable.yukihookapi.hook.log.YLog

data class Field(val name: String?)

data class Method(val name: String?, val parameters: List<String>?)

/**
 * Resolves a type name string into a format directly recognizable by reflection systems like KavaRef.
 *
 * @return Class for primitive type names, or the original String otherwise.
 */
fun String.toClassIfPrimitiveElseString(): Any = when (this) {
    // KavaRef's parameters() accepts Class or String. When given a String,
    // it calls Class.forName() internally, which cannot resolve bare primitive
    // type names like "int" or "boolean" and throws ClassNotFoundException.
    // To work around this, we convert primitive type names to their corresponding
    // java.lang.Class instances (e.g. "int" -> Integer.TYPE) so KavaRef hits
    // the Class branch directly. Ordinary class names like "java.lang.String"
    // are left as raw String, letting KavaRef resolve them via Class.forName()
    // as intended.
    "boolean" -> Boolean::class.java

    "byte" -> Byte::class.java

    "char" -> Char::class.java

    "short" -> Short::class.java

    "int" -> Int::class.java

    "long" -> Long::class.java

    "float" -> Float::class.java

    "double" -> Double::class.java

    "void" -> Void::class.java

    else -> this
}

fun Array<String>.toClassIfPrimitiveElseString(): Array<Any> = Array(size) {
    this[it].toClassIfPrimitiveElseString()
}

fun List<String>.toClassIfPrimitiveElseString(): Array<Any> = Array(size) {
    this[it].toClassIfPrimitiveElseString()
}

fun Any.resolveMethod(method: Method): MethodResolver<*>? {
    if (method.name.isNullOrBlank()) {
        YLog.error("cannot determine which method to resolve on ${this::class.simpleName}, name is null or blank")
        return null
    }
    return runCatching {
        this.asResolver().firstMethodOrNull {
            name = method.name
            method.parameters?.let {
                parameters(*it.toClassIfPrimitiveElseString())
            }
            superclass()
        }
    }.onFailure {
        YLog.error("resolve failed: ${this::class.simpleName}.${method.name}(${method.parameters})")
    }.getOrNull()
        .also {
            if (it == null) {
                YLog.error("method not found: ${this::class.simpleName}.${method.name}(${method.parameters})")
            }
        }
}

fun Any.resolveField(field: Field): FieldResolver<*>? {
    if (field.name.isNullOrBlank()) {
        YLog.error("cannot determine which field to resolve on ${this::class.simpleName}, name is null or blank")
        return null
    }
    return runCatching {
        this.asResolver().firstFieldOrNull {
            name = field.name
            superclass()
        }
    }.onFailure {
        YLog.error("resolve failed: ${this::class.simpleName}.${field.name}")
    }.getOrNull()
        .also {
            if (it == null) {
                YLog.error("field not found: ${this::class.simpleName}.${field.name}")
            }
        }
}

fun Class<*>.resolveMethod(method: Method): MethodResolver<*>? {
    if (method.name.isNullOrBlank()) {
        YLog.error("cannot determine which method to resolve on ${this.simpleName}, name is null or blank")
        return null
    }
    return runCatching {
        this.resolve().firstMethodOrNull {
            name = method.name
            method.parameters?.let {
                parameters(*it.toClassIfPrimitiveElseString())
            }
            superclass()
        }
    }.onFailure {
        YLog.error("resolve failed: ${this.simpleName}.${method.name}(${method.parameters})")
    }.getOrNull()
        .also {
            if (it == null) {
                YLog.error("method not found: ${this.simpleName}.${method.name}(${method.parameters})")
            }
        }
}

fun Class<*>.resolveField(field: Field): FieldResolver<*>? {
    if (field.name.isNullOrBlank()) {
        YLog.error("cannot determine which field to resolve on ${this.simpleName}, name is null or blank")
        return null
    }
    return runCatching {
        this.resolve().firstFieldOrNull {
            name = field.name
            superclass()
        }
    }.onFailure {
        YLog.error("resolve failed: ${this.simpleName}.${field.name}")
    }.getOrNull()
        .also {
            if (it == null) {
                YLog.error("field not found: ${this.simpleName}.${field.name}")
            }
        }
}

inline fun <reified T> Any.invokeMethod(method: Method, vararg args: Any?): T? = this.resolveMethod(method)?.invoke(*args) as? T

inline fun <reified T> Class<*>.invokeStaticMethod(method: Method, vararg args: Any?): T? = this.resolveMethod(method)?.invoke(*args) as? T

inline fun <reified T> Any.getField(field: Field): T? = this.resolveField(field)?.get() as? T

fun <T> Any.setField(field: Field, value: T?) {
    this.resolveField(field)?.set(value)
}

inline fun <reified T> Class<*>.getStaticField(field: Field): T? = this.resolveField(field)?.get() as? T

fun <T> Class<*>.setStaticField(field: Field, value: T?) {
    this.resolveField(field)?.set(value)
}
