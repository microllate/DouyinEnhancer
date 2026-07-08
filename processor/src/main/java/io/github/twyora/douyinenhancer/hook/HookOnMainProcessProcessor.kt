package io.github.twyora.douyinenhancer.hook

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.ksp.writeTo

class HookOnMainProcessProcessor(private val codeGenerator: CodeGenerator) : SymbolProcessor {
    private var hasGenerated = false

    override fun process(resolver: Resolver): List<KSAnnotated> {
        if (hasGenerated) {
            return emptyList()
        }

        val hookClasses = resolver.getSymbolsWithAnnotation(
            "io.github.twyora.douyinenhancer.hook.HookOnMainProcess"
        ).filterIsInstance<KSClassDeclaration>().toList()

        val hookerNames = hookClasses.map {
            it.qualifiedName!!.asString()
        }

        val fileSpec = FileSpec.builder(
            packageName = "io.github.twyora.douyinenhancer.hook",
            fileName = "HookerRegistry"
        ).addType(
            TypeSpec.objectBuilder("HookerRegistry").addProperty(
                PropertySpec.builder(
                    name = "mainProcessHookers",
                    type = List::class.asClassName()
                        .parameterizedBy(ClassName("com.highcapable.yukihookapi.hook.entity", "YukiBaseHooker"))
                ).initializer(
                    "listOf(%L)",
                    hookerNames.joinToString(",\n") {
                        it
                    }
                ).build()
            ).build()
        ).build()

        fileSpec.writeTo(
            codeGenerator = codeGenerator,
            dependencies = Dependencies(aggregating = true)
        )

        hasGenerated = true

        return emptyList()
    }
}