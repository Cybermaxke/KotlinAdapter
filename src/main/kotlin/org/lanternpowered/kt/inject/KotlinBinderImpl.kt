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
import com.google.inject.TypeLiteral
import com.google.inject.binder.AnnotatedBindingBuilder
import com.google.inject.binder.AnnotatedConstantBindingBuilder
import com.google.inject.binder.AnnotatedElementBuilder
import com.google.inject.binder.ConstantBindingBuilder
import com.google.inject.binder.LinkedBindingBuilder
import com.google.inject.binder.ScopedBindingBuilder
import org.lanternpowered.kt.typeLiteral
import kotlin.reflect.KClass

internal class PrivateKotlinBinderImpl(private val binder: PrivateBinder) : PrivateKotlinBinder(), PrivateBinder by binder {

    override fun <T> bind(key: Key<T>) = KLinkedBindingBuilderImpl(this.binder.bind(key))
    override fun <T> bind(typeLiteral: TypeLiteral<T>) = KAnnotatedBindingBuilderImpl(this.binder.bind(typeLiteral))
    override fun <T> bind(type: Class<T>) = KAnnotatedBindingBuilderImpl(this.binder.bind(type))
    override fun <T> bind(typeToken: TypeToken<T>): KAnnotatedBindingBuilder<T> = bind(typeToken.typeLiteral)

    override fun bindConstant() = KAnnotatedConstantBindingBuilderImpl(this.binder.bindConstant())

    override fun <T : Any> getProvider(kClass: KClass<T>): Provider<T> = getProvider(Key.get(kClass.java))
    override fun <T> getProvider(typeLiteral: TypeLiteral<T>): Provider<T> = getProvider(Key.get(typeLiteral))
    override fun <T> getProvider(typeToken: TypeToken<T>): Provider<T> = getProvider(Key.get(typeToken.typeLiteral))

    override fun <T : Any> getMembersInjector(kClass: KClass<T>): MembersInjector<T> = getMembersInjector(kClass.java)
    override fun <T> getMembersInjector(typeToken: TypeToken<T>): MembersInjector<T> = getMembersInjector(typeToken.typeLiteral)

    override fun expose(type: KClass<*>): AnnotatedElementBuilder = expose(type.java)
    override fun expose(type: TypeToken<*>): AnnotatedElementBuilder = expose(type.typeLiteral)
}

internal class KotlinBinderImpl(internal val binder: Binder) : KotlinBinder(), Binder by binder {

    override fun <T> bind(key: Key<T>) = KLinkedBindingBuilderImpl(this.binder.bind(key))
    override fun <T> bind(typeLiteral: TypeLiteral<T>) = KAnnotatedBindingBuilderImpl(this.binder.bind(typeLiteral))
    override fun <T> bind(type: Class<T>) = KAnnotatedBindingBuilderImpl(this.binder.bind(type))
    override fun <T> bind(typeToken: TypeToken<T>) = bind(typeToken.typeLiteral)

    override fun bindConstant() = KAnnotatedConstantBindingBuilderImpl(this.binder.bindConstant())

    override fun <T : Any> getProvider(kClass: KClass<T>): Provider<T> = getProvider(Key.get(kClass.java))
    override fun <T> getProvider(typeLiteral: TypeLiteral<T>): Provider<T> = getProvider(Key.get(typeLiteral))
    override fun <T> getProvider(typeToken: TypeToken<T>): Provider<T> = getProvider(Key.get(typeToken.typeLiteral))

    override fun <T : Any> getMembersInjector(kClass: KClass<T>): MembersInjector<T> = getMembersInjector(kClass.java)
    override fun <T> getMembersInjector(typeToken: TypeToken<T>): MembersInjector<T> = getMembersInjector(typeToken.typeLiteral)
}

internal class KLinkedBindingBuilderImpl<T>(
        private val builder: LinkedBindingBuilder<T>
) : KLinkedBindingBuilder<T>(), LinkedBindingBuilder<T> by builder {

    override fun <T : Any> to(implementation: KClass<out T>): ScopedBindingBuilder {
        return (this as LinkedBindingBuilder<Any?>).to(implementation.java as Class<out Any?>)
    }

    override fun <R : T> toProvider(provider: () -> R): ScopedBindingBuilder = toProvider(Provider(provider))
}

internal class KAnnotatedBindingBuilderImpl<T>(
        private val builder: AnnotatedBindingBuilder<T>
) : KAnnotatedBindingBuilder<T>(), AnnotatedBindingBuilder<T> by builder {

    override fun annotatedWith(annotationType: Class<out Annotation>) =
            this.builder.annotatedWith(annotationType).run { this as? KLinkedBindingBuilder<T> ?: KLinkedBindingBuilderImpl(this) }

    override fun annotatedWith(annotation: Annotation) =
            this.builder.annotatedWith(annotation).run { this as? KLinkedBindingBuilder<T> ?: KLinkedBindingBuilderImpl(this) }

    override fun <T : Any> to(implementation: KClass<out T>): ScopedBindingBuilder {
        return (this as LinkedBindingBuilder<Any?>).to(implementation.java as Class<out Any?>)
    }

    override fun annotatedWith(annotationType: KClass<out Annotation>) = annotatedWith(annotationType.java)

    override fun <R : T> toProvider(provider: () -> R): ScopedBindingBuilder = toProvider(Provider(provider))
}

internal class KAnnotatedConstantBindingBuilderImpl(
        private val builder: AnnotatedConstantBindingBuilder
) : KAnnotatedConstantBindingBuilder(), AnnotatedConstantBindingBuilder by builder {

    override fun annotatedWith(annotationType: Class<out Annotation>) =
            this.builder.annotatedWith(annotationType).run { this as? KConstantBindingBuilder ?: KConstantBindingBuilderImpl(this) }

    override fun annotatedWith(annotation: Annotation) =
            this.builder.annotatedWith(annotation).run { this as? KConstantBindingBuilder ?: KConstantBindingBuilderImpl(this) }

    override fun annotatedWith(annotationType: KClass<out Annotation>): KConstantBindingBuilder = annotatedWith(annotationType.java)
}

internal class KConstantBindingBuilderImpl(
        private val builder: ConstantBindingBuilder
) : KConstantBindingBuilder(), ConstantBindingBuilder by builder {

    override fun to(value: KClass<*>) {
        to(value.java)
    }
}
