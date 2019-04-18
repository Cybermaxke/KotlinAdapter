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
package org.lanternpowered.kt.inject

import com.google.common.reflect.TypeToken
import com.google.inject.Binder
import com.google.inject.BindingAnnotation
import com.google.inject.Injector
import com.google.inject.Key
import com.google.inject.MembersInjector
import com.google.inject.Module
import com.google.inject.TypeLiteral
import com.google.inject.matcher.Matchers
import com.google.inject.name.Names
import com.google.inject.spi.TypeEncounter
import com.google.inject.spi.TypeListener
import org.lanternpowered.lmbda.kt.createLambda
import org.lanternpowered.lmbda.kt.privateLookupIn
import java.lang.invoke.MethodHandles
import java.lang.reflect.Modifier
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.jvm.javaField
import kotlin.reflect.jvm.javaType

class InjectablePropertyModule : Module, TypeListener {

    override fun configure(binder: Binder) {
        binder.bindListener(Matchers.any(), this)
    }

    override fun <I : Any> hear(type: TypeLiteral<I>, encounter: TypeEncounter<I>) {
        var javaTarget = type.rawType as Class<*>
        while (javaTarget != Any::class.java && !javaTarget.isArray && !javaTarget.isPrimitive) {
            try {
                val target = javaTarget.kotlin
                val lookup by lazy { lookup.privateLookupIn(javaTarget) }

                target.declaredMemberProperties.forEach { property ->
                    val field = property.javaField
                    if (field != null && InjectedProperty::class.java.isAssignableFrom(field.type)) {
                        // Found a valid field, register a member injector

                        // Extract the value type from the property
                        val valueType = TypeLiteral.get(property.returnType.javaType)

                        // Search for binding annotations
                        val bindingAnnotations = property.annotations.filter { it.annotationClass.findAnnotation<BindingAnnotation>() != null }
                        val key = when {
                            bindingAnnotations.size > 1 -> throw IllegalStateException("Only one BindingAnnotation is allowed on: $property")
                            bindingAnnotations.size == 1 -> {
                                var bindingAnnotation = bindingAnnotations[0]
                                // Translate the kotlin named to the guice one
                                if (bindingAnnotation is javax.inject.Named) {
                                    bindingAnnotation = Names.named(bindingAnnotation.value)
                                }
                                Key.get(valueType, bindingAnnotation)
                            }
                            else -> Key.get(valueType)
                        }

                        // Kotlin property fields can be static (for objects) or non-static, we need
                        // to consider both when performing injections
                        val getterHandle = lookup.unreflectGetter(field)
                        val getter: (Any) -> InjectedProperty<Any> = if (Modifier.isStatic(field.modifiers)) {
                            val supplier: () -> InjectedProperty<Any> = getterHandle.createLambda()
                            val fn = { _: Any -> supplier() }
                            fn
                        } else getterHandle.createLambda()

                        val injectorProvider = encounter.getProvider(Injector::class.java)
                        encounter.register(MembersInjector {
                            val injector = injectorProvider.get()
                            val propInstance = getter(it)

                            val originalPoint = InjectablePropertyPoint.get()

                            val source = TypeToken.of(field.declaringClass)
                            val propertyType = TypeToken.of(property.returnType.javaType)
                            val annotations = property.annotations.toTypedArray()

                            InjectablePropertyPoint.set(InjectionPointImpl.KProperty(source, propertyType, annotations, key, property))
                            try {
                                propInstance.inject { injector.getInstance(key) }
                            } finally {
                                if (originalPoint != null) {
                                    InjectablePropertyPoint.set(originalPoint)
                                } else {
                                    InjectablePropertyPoint.remove()
                                }
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
