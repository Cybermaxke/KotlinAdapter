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
@file:Suppress("NOTHING_TO_INLINE", "UNCHECKED_CAST")

package org.lanternpowered.kt

import com.google.inject.Scope
import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import org.lanternpowered.kt.inject.InjectedProperty
import org.lanternpowered.kt.inject.DefaultInjectedProperty
import org.lanternpowered.kt.inject.LazyInjectedProperty
import kotlin.reflect.KClass

/**
 * Creates a new instance of the [InjectedProperty] that can be used
 * to mark a property for injection by guice. This injection will be
 * done lazily.
 */
inline fun <reified T> injectLazily() = LazyInjectedProperty<T>()

/**
 * Creates a new instance of the [InjectedProperty] that can be used
 * to mark a property for injection by guice.
 */
inline fun <reified T> inject() = DefaultInjectedProperty<T>()

inline fun ScopedBindingBuilder.inScope(scope: Scope) = `in`(scope)
inline fun ScopedBindingBuilder.inScope(scopeAnnotation: Class<out Annotation>) = `in`(scopeAnnotation)
inline fun ScopedBindingBuilder.inScope(scopeAnnotation: KClass<out Annotation>) = `in`(scopeAnnotation.java)

inline fun <reified A> AnnotatedBindingBuilder<*>.annotatedWith(): LinkedBindingBuilder<*> = annotatedWith(A::class.java as Class<out Annotation>)
