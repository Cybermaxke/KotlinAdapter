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

import com.google.common.reflect.TypeToken
import com.google.inject.Binder
import com.google.inject.Key
import com.google.inject.MembersInjector
import com.google.inject.PrivateBinder
import com.google.inject.Provider
import com.google.inject.Scope
import com.google.inject.TypeLiteral
import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.binder.AnnotatedConstantBindingBuilder
import com.google.inject.binder.AnnotatedElementBuilder
import com.google.inject.binder.ConstantBindingBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import kotlin.reflect.KClass

inline fun <reified T> bindingKey(): Key<T> = Key.get(object : TypeLiteral<T>() {})

/**
 * A [PrivateBinder] with extensions for Kotlin.
 */
abstract class PrivateKotlinBinder : KotlinBinder(), PrivateBinder {

    /**
     * See [PrivateBinder.expose].
     */
    abstract fun expose(type: KClass<*>): AnnotatedElementBuilder

    /**
     * See [PrivateBinder.expose].
     */
    abstract fun expose(type: TypeToken<*>): AnnotatedElementBuilder

    /**
     * See [PrivateBinder.expose].
     */
    inline fun <reified T> expose() = expose(object : TypeLiteral<T>() {})
}

/**
 * A [Binder] with extensions for Kotlin.
 */
abstract class KotlinBinder : Binder {

    abstract override fun <T> bind(key: Key<T>): KLinkedBindingBuilder<T>
    abstract override fun <T> bind(typeLiteral: TypeLiteral<T>): KAnnotatedBindingBuilder<T>
    abstract override fun <T> bind(type: Class<T>): KAnnotatedBindingBuilder<T>

    abstract override fun bindConstant(): KAnnotatedConstantBindingBuilder

    /**
     * Binds the specified [TypeToken].
     */
    abstract fun <T> bind(typeToken: TypeToken<T>): KAnnotatedBindingBuilder<T>

    /**
     * Binds the specified type [T].
     */
    inline fun <reified T> bind() = bind(object : TypeLiteral<T>() {})

    /**
     * See [Binder.getProvider].
     */
    abstract fun <T : Any> getProvider(kClass: KClass<T>): Provider<T>

    /**
     * See [Binder.getProvider].
     */
    abstract fun <T> getProvider(typeLiteral: TypeLiteral<T>): Provider<T>

    /**
     * See [Binder.getProvider].
     */
    abstract fun <T> getProvider(typeToken: TypeToken<T>): Provider<T>

    /**
     * See [Binder.getProvider].
     */
    inline fun <reified T> getProvider(): Provider<T> = getProvider(object : TypeLiteral<T>() {})

    /**
     * See [Binder.getMembersInjector].
     */
    abstract fun <T : Any> getMembersInjector(kClass: KClass<T>): MembersInjector<T>

    /**
     * See [Binder.getMembersInjector].
     */
    abstract fun <T> getMembersInjector(typeToken: TypeToken<T>): MembersInjector<T>

    /**
     * See [Binder.getMembersInjector].
     */
    inline fun <reified T> getMembersInjector(): MembersInjector<T> = getMembersInjector(object : TypeLiteral<T>() {})
}

abstract class KAnnotatedBindingBuilder<T> : KLinkedBindingBuilder<T>(), AnnotatedBindingBuilder<T> {

    abstract override fun annotatedWith(annotationType: Class<out Annotation>): KLinkedBindingBuilder<T>
    abstract override fun annotatedWith(annotation: Annotation): KLinkedBindingBuilder<T>

    /**
     * See [AnnotatedBindingBuilder.annotatedWith].
     */
    abstract fun annotatedWith(annotationType: KClass<out Annotation>): KLinkedBindingBuilder<T>

    /**
     * See [AnnotatedBindingBuilder.annotatedWith].
     */
    inline fun <reified A : Annotation> annotatedWith() = annotatedWith(A::class)
}

abstract class KLinkedBindingBuilder<T> : LinkedBindingBuilder<T> {

    fun inScope(scope: Scope) = `in`(scope)
    fun inScope(scopeAnnotation: Class<out Annotation>) = `in`(scopeAnnotation)
    fun inScope(scopeAnnotation: KClass<out Annotation>) = inScope(scopeAnnotation.java)

    /**
     * Use the specified scope type [T].
     */
    inline fun <reified T : Annotation> inScope() = inScope(T::class)

    /**
     * See [LinkedBindingBuilder.to].
     */
    abstract fun <T : Any> to(implementation: KClass<out T>): ScopedBindingBuilder

    /**
     * See [LinkedBindingBuilder.to].
     */
    inline fun <reified T> to(): ScopedBindingBuilder = (this as LinkedBindingBuilder<Any?>).to(object : TypeLiteral<T>() {} as TypeLiteral<out Any?>)

    /**
     * See [LinkedBindingBuilder.toProvider].
     */
    abstract fun <R : T> toProvider(provider: () -> R): ScopedBindingBuilder
}

abstract class KAnnotatedConstantBindingBuilder : AnnotatedConstantBindingBuilder {

    abstract override fun annotatedWith(annotationType: Class<out Annotation>): KConstantBindingBuilder
    abstract override fun annotatedWith(annotation: Annotation): KConstantBindingBuilder

    /**
     * See [AnnotatedConstantBindingBuilder.annotatedWith].
     */
    abstract fun annotatedWith(annotationType: KClass<out Annotation>): KConstantBindingBuilder

    /**
     * See [AnnotatedConstantBindingBuilder.annotatedWith].
     */
    inline fun <reified A : Annotation> annotatedWith() = annotatedWith(A::class)
}

abstract class KConstantBindingBuilder : ConstantBindingBuilder {

    /**
     * See [ConstantBindingBuilder.to].
     */
    abstract fun to(value: KClass<*>)

    /**
     * See [ConstantBindingBuilder.to].
     */
    inline fun <reified T> to() = to(T::class)
}
