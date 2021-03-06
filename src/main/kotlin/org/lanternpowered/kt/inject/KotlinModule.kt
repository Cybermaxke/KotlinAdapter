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
import com.google.inject.AbstractModule
import com.google.inject.Key
import com.google.inject.TypeLiteral
import org.lanternpowered.kt.typeLiteral

abstract class KotlinModule : AbstractModule() {

    private var kotlinBinder: KotlinBinderImpl? = null

    /**
     * Gets the [KotlinBinder].
     */
    override fun binder(): KotlinBinder {
        val binder = super.binder()
        var kotlinBinder = this.kotlinBinder
        if (kotlinBinder == null || kotlinBinder.binder != binder) {
            kotlinBinder = binder as? KotlinBinderImpl ?: KotlinBinderImpl(binder)
        }
        return kotlinBinder
    }

    override fun <T> bind(key: Key<T>) = binder().bind(key)
    override fun <T> bind(typeLiteral: TypeLiteral<T>) = binder().bind(typeLiteral)
    override fun <T> bind(type: Class<T>) = binder().bind(type)

    /**
     * Binds the specified [TypeToken].
     */
    protected fun <T> bind(typeToken: TypeToken<T>): KAnnotatedBindingBuilder<T> = bind(typeToken.typeLiteral)

    /**
     * Binds the specified type [T].
     */
    protected inline fun <reified T> bind() = bind(object : TypeLiteral<T>() {})
}
