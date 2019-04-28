/*
 * This file is part of KotlinAdapter, licensed under the MIT License (MIT).
 *
 * Copyright (c) LanternPowered <https://www.lanternpowered.org/>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the Software), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED AS IS, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
@file:Suppress("UNCHECKED_CAST")

package org.lanternpowered.kt.inject

import com.google.inject.Binder
import com.google.inject.BindingAnnotation
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.MembersInjector
import com.google.inject.Module
import com.google.inject.TypeLiteral
import com.google.inject.matcher.Matchers
import com.google.inject.spi.TypeEncounter
import com.google.inject.spi.TypeListener
import org.lanternpowered.kt.typeLiteral
import org.lanternpowered.kt.typeToken
import org.lanternpowered.lmbda.kt.createLambda
import org.lanternpowered.lmbda.kt.privateLookupIn
import java.lang.invoke.MethodHandles
import java.lang.reflect.Field
import java.lang.reflect.Modifier
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField

class KotlinInjectionModule : Module, TypeListener {

    override fun configure(binder: Binder) {
        binder.bindListener(Matchers.any(), this)
    }

    private fun createSupplier(lookup: MethodHandles.Lookup, field: Field): (Any) -> Any {
        // Kotlin property fields can be static (for objects) or non-static, we need
        // to consider both when performing injections
        val getterHandle = lookup.unreflectGetter(field)
        return if (Modifier.isStatic(field.modifiers)) {
            val supplier: () -> Any = getterHandle.createLambda()
            val fn = { _: Any -> supplier() }
            fn
        } else getterHandle.createLambda()
    }

    private fun findBindingAnnotation(annotations: Array<Annotation>, message: () -> String): Annotation? {
        val bindingAnnotations = annotations.filter { it.annotationClass.findAnnotation<BindingAnnotation>() != null }
        return when {
            bindingAnnotations.size > 1 -> throw IllegalStateException(message())
            bindingAnnotations.size == 1 -> bindingAnnotations[0]
            else -> null
        }
    }

    private fun withInjectionPoint(key: Key<*>, annotations: Array<Annotation>, source: Class<*>, fn: () -> Unit) {
        val originalPoint = injectablePropertyPoint.get()
        val propertyType = key.typeLiteral.typeToken

        injectablePropertyPoint.set(InjectionPointImpl.KProperty(source.typeToken, propertyType, annotations, key))
        try {
            fn()
        } finally {
            if (originalPoint != null) {
                injectablePropertyPoint.set(originalPoint)
            } else {
                injectablePropertyPoint.remove()
            }
        }
    }

    override fun <I : Any> hear(type: TypeLiteral<I>, encounter: TypeEncounter<I>) {
        var javaTarget = type.rawType as Class<*>
        while (javaTarget != Any::class.java && !javaTarget.isArray && !javaTarget.isPrimitive) {
            try {
                val target = javaTarget.kotlin
                val lookup by lazy { lookup.privateLookupIn(javaTarget) }

                javaTarget.declaredFields.forEach { field ->
                    if (!field.name.startsWith("$\$delegate_")) {
                        return@forEach
                    }
                    val injectorProvider = encounter.getProvider(Injector::class.java)
                    val getter = createSupplier(lookup, field)

                    val annotatedType = target.supertypes.find { it.typeToken.rawType == field.type }
                    val annotations = annotatedType?.annotations?.toTypedArray() ?: arrayOf()

                    val valueType = annotatedType?.typeLiteral ?: field.type.typeLiteral
                    val bindingAnnotation = findBindingAnnotation(annotations) {
                        "Only one BindingAnnotation is allowed on the delegate interface '${field.type.simpleName}'"
                    }
                    val key = if (bindingAnnotation == null) Key.get(valueType) else Key.get(valueType, bindingAnnotation)

                    encounter.register(MembersInjector {
                        val delegateInstance = getter(it) as? InjectedDelegate ?: return@MembersInjector
                        val injector = injectorProvider.get()

                        withInjectionPoint(key, annotations, field.declaringClass) {
                            delegateInstance.injectDelegateObject(injector.getInstance(key))
                        }
                    })
                }

                target.declaredMemberProperties.forEach { property ->
                    val field = property.javaField
                    if (field != null && InjectedProperty::class.java.isAssignableFrom(field.type)) {
                        // Found a valid field, register a member injector
                        val annotations = property.annotations.toTypedArray()
                        val bindingAnnotation = findBindingAnnotation(annotations) {
                            "Only one BindingAnnotation is allowed on the property '${property.name}'"
                        }

                        val getter = createSupplier(lookup, field)
                        val injectorProvider = encounter.getProvider(Injector::class.java)
                        encounter.register(MembersInjector {
                            val injector = injectorProvider.get()
                            val propInstance = getter(it) as InternalInjectedProperty<*, Any?>

                            val valueType = propInstance.getInjectedType(property)
                            val key = if (bindingAnnotation == null) Key.get(valueType) else Key.get(valueType, bindingAnnotation)

                            withInjectionPoint(key, annotations, field.declaringClass) {
                                propInstance.inject(property) { injector.getInstance(key) }
                            }
                        })
                    }
                }
            } catch (e: UnsupportedOperationException) {
            } catch (e: Error) {
                if (e.javaClass.name != "kotlin.reflect.jvm.internal.KotlinReflectionInternalError") {
                    throw e
                }
            }
            javaTarget = javaTarget.superclass
        }
    }

    companion object {

        private val lookup = MethodHandles.lookup()
    }
}
