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

package org.lanternpowered.kt.inject

import com.google.inject.Scope
import com.google.inject.Provider
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import org.lanternpowered.kt.plugin.TestInterfaceImpl
import org.spongepowered.api.inject.InjectionPoint
import kotlin.reflect.KClass

/**
 * Creates a new instance of the [InjectedProperty] that can be used
 * to mark a property for injection by guice. This injection will be
 * done lazily.
 */
inline fun <reified T> lazyInject(): InjectedProperty<T> = SynchronizedLazyInjectedProperty()

/**
 * Creates a new instance of the [InjectedProperty] that can be used
 * to mark a property for injection by guice. This injection will be
 * done lazily.
 */
inline fun <reified T> lazyInject(safetyMode: LazyThreadSafetyMode): InjectedProperty<T> = LazyInjectedProperty(safetyMode)

/**
 * Creates a new instance of the [InjectedProperty] that can be used
 * to mark a property for injection by guice.
 */
inline fun <reified T> inject(): InjectedProperty<T> = DefaultInjectedProperty()

/**
 * Creates a new instance of the [InjectedProperty]. The property will use
 * a [Provider] on the background, this makes that returned values by the
 * property may be different instances every time the property gets accessed.
 */
inline fun <reified T> injectProvider(): InjectedProperty<T> = ProviderInjectedProperty()

/**
 * Creates a new instance of the [InjectedProperty] to inject [InjectionPoint]s.
 */
inline fun injectionPoint(): InjectedProperty<InjectionPoint> = ProviderInjectedProperty()

/**
 * Creates a new injectable delegation.
 */
inline fun <reified T> injectDelegate(): T {
    return DelegateObjectGenerator.generateDelegateObject(T::class.java)
}

inline fun ScopedBindingBuilder.inScope(scope: Scope) = `in`(scope)
inline fun ScopedBindingBuilder.inScope(scopeAnnotation: Class<out Annotation>) = `in`(scopeAnnotation)
inline fun ScopedBindingBuilder.inScope(scopeAnnotation: KClass<out Annotation>) = `in`(scopeAnnotation.java)
