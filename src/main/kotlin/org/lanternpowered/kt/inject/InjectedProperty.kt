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
@file:Suppress("UNCHECKED_CAST", "FunctionName")

package org.lanternpowered.kt.inject

import com.google.common.reflect.TypeParameter
import com.google.common.reflect.TypeToken
import com.google.inject.Provider
import com.google.inject.TypeLiteral
import org.lanternpowered.kt.typeLiteral
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
    abstract fun inject(property: KProperty<*>, provider: () -> I)

    /**
     * Throws a exception caused by the injected property not being injected.
     */
    internal fun throwNotInjected(property: KProperty<*>): Nothing =
            throw IllegalStateException("The property '${property.name}' is not yet injected.")

    internal fun throwNullValue(property: KProperty<*>): Nothing =
            throw IllegalStateException("The property '${property.name}' received a null value.")
}

@PublishedApi internal open class ProviderInjectedProperty<T> : InternalInjectedProperty<T, Provider<T>>() {

    private var provider: Any? = Uninitialized

    override val initialized
        get() = this.provider !== Uninitialized

    override fun getInjectedType(property: KProperty<*>): TypeLiteral<Provider<T>> = getProviderType(property)

    override fun inject(property: KProperty<*>, provider: () -> Provider<T>) {
        this.provider = provider()
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val provider = this.provider
        if (provider == Uninitialized) throwNotInjected(property)
        val value = (provider as Provider<T>).get()
        if (!property.returnType.isMarkedNullable && value == null) throwNullValue(property)
        return value
    }

    private fun <X> getProviderType(property: KProperty<*>): TypeLiteral<Provider<X>>
            = object : TypeToken<Provider<X>>() {}.where(object : TypeParameter<X>() {}, property.returnType.typeToken as TypeToken<X>).typeLiteral

}

@PublishedApi internal abstract class SimpleInjectedProperty<T> : InternalInjectedProperty<T, T>() {

    override fun getInjectedType(property: KProperty<*>): TypeLiteral<T> = property.returnType.typeLiteral as TypeLiteral<T>
}

/**
 * The default injected property type, directly initializes the value.
 */
@PublishedApi internal class DefaultInjectedProperty<T> : SimpleInjectedProperty<T>() {

    private var value: Any? = Uninitialized

    override val initialized
        get() = this.value !== Uninitialized

    override fun inject(property: KProperty<*>, provider: () -> T) {
        val value = provider()
        if (!property.returnType.isMarkedNullable && value == null) throwNullValue(property)
        this.value = value
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val value = this.value
        return if (value !== Uninitialized) value as T else throwNotInjected(property)
    }
}

@PublishedApi internal fun <T> LazyInjectedProperty(safetyMode: LazyThreadSafetyMode): InjectedProperty<T> {
    return when (safetyMode) {
        LazyThreadSafetyMode.SYNCHRONIZED -> SynchronizedLazyInjectedProperty()
        LazyThreadSafetyMode.PUBLICATION -> SynchronizedLazyInjectedProperty() // TODO?
        LazyThreadSafetyMode.NONE -> LazyInjectedProperty()
    }
}

/**
 * The lazy injected property type, the value will be
 * initialized the first time it's requested.
 */
internal open class LazyInjectedProperty<T> : SimpleInjectedProperty<T>() {

    private var provider: Any? = Uninitialized
    internal var value: Any? = Uninitialized

    override val initialized
        get() = this.value !== Uninitialized

    override fun inject(property: KProperty<*>, provider: () -> T) {
        this.provider = provider
        this.value = Uninitialized
    }

    override fun getValue(thisRef: Any, property: KProperty<*>) = getValue(property)

    internal fun getValue(property: KProperty<*>): T {
        var value = this.value
        if (value !== Uninitialized) {
            return value as T
        }
        val provider = this.provider
        if (provider !== Uninitialized) {
            value = if (provider != null) (provider as () -> T)() else null
            if (!property.returnType.isMarkedNullable && value == null) throwNullValue(property)
            this.value = value as T
            this.provider = Uninitialized // Cleanup
            return value
        }
        throwNotInjected(property)
    }
}

@PublishedApi internal class SynchronizedLazyInjectedProperty<T> : LazyInjectedProperty<T>() {

    private val lock = Object()

    override fun getValue(thisRef: Any, property: KProperty<*>): T {
        val value = this.value
        if (value !== Uninitialized) {
            return value as T
        }
        synchronized(this.lock) {
            return getValue(property)
        }
    }
}

internal object Uninitialized
