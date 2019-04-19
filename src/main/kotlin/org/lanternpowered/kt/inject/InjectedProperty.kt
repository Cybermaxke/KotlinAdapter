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

import com.google.common.reflect.TypeParameter
import com.google.common.reflect.TypeToken
import com.google.inject.Provider
import com.google.inject.TypeLiteral
import org.lanternpowered.kt.typeLiteral
import org.lanternpowered.kt.typeParameter
import org.lanternpowered.kt.typeToken
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * A property that can be injected by guice.
 */
interface InjectedProperty<T> : ReadOnlyProperty<Any, T> {

    /**
     * Whether this property is already injected.
     */
    val initialized: Boolean
}

@PublishedApi internal abstract class InternalInjectedProperty<T, I> : InjectedProperty<T> {

    /**
     * The type of the object that will be injected into this property.
     */
    abstract fun getInjectedType(property: KProperty<*>): TypeLiteral<I>

    /**
     * Injects the given value.
     */
    abstract fun inject(provider: () -> I)

    /**
     * Throws a exception caused by the injected property not being injected.
     */
    internal fun throwNotInjected(property: KProperty<*>): Nothing =
            throw IllegalStateException("The ${property.name} property is not yet injected.")
}

@PublishedApi internal open class ProviderInjectedProperty<T> : InternalInjectedProperty<T, Provider<T>>() {

    private var provider: Provider<T>? = null

    override val initialized
        get() = this.provider !== null

    override fun getInjectedType(property: KProperty<*>) = getProviderType<T>(property)

    override fun inject(provider: () -> Provider<T>) {
        this.provider = provider()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val provider = this.provider
        return if (provider != null) provider.get() else throwNotInjected(property)
    }

    private fun <X> getProviderType(property: KProperty<*>): TypeLiteral<Provider<X>>
            = object : TypeToken<Provider<X>>() {}.where(object : TypeParameter<X>() {}, property.returnType.typeToken as TypeToken<X>).typeLiteral

}
@PublishedApi internal class NonNullInjectedProperty<T>(private val message: (KProperty<*>) -> String) : ProviderInjectedProperty<T>() {

    override fun getValue(thisRef: Any, property: KProperty<*>) =
            super.getValue(thisRef, property) ?: throw IllegalStateException(this.message(property))
}

@PublishedApi internal abstract class SimpleInjectedProperty<T> : InternalInjectedProperty<T, T>() {

    override fun getInjectedType(property: KProperty<*>) = property.returnType.typeLiteral as TypeLiteral<T>
}

/**
 * The default injected property type, directly initializes the value.
 */
@PublishedApi internal class DefaultInjectedProperty<T> : SimpleInjectedProperty<T>() {

    private var value: Any? = uninitialized

    override val initialized
        get() = this.value !== uninitialized

    override fun inject(provider: () -> T) {
        this.value = provider()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val value = this.value
        return if (value !== uninitialized) value as T else throwNotInjected(property)
    }
}

/**
 * The lazy injected property type, the value will be
 * initialized the first time it's requested.
 */
@PublishedApi internal class LazyInjectedProperty<T> : SimpleInjectedProperty<T>() {

    private var provider: (() -> T)? = null
    private var value: Any? = uninitialized

    override val initialized
        get() = this.value !== uninitialized

    override fun inject(provider: () -> T) {
        this.provider = provider
        this.value = uninitialized
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val value = this.value
        if (value !== uninitialized) {
            return value as T
        }
        val provider = this.provider
        if (provider != null) {
            return provider().also { this.value = it }
        }
        throwNotInjected(property)
    }
}

internal val uninitialized = Object()
