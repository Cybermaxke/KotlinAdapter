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

import com.google.inject.AbstractModule
import com.google.inject.Module
import com.google.inject.PrivateModule
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Constructs a new [Module].
 */
fun module(fn: KotlinBinder.() -> Unit): Module {
    contract {
        callsInPlace(fn, InvocationKind.EXACTLY_ONCE)
    }
    return object : AbstractModule() {
        override fun configure() {
            fn(binder() as? KotlinBinder ?: KotlinBinderImpl(binder()))
        }
    }
}

/**
 * Constructs a new [Module].
 */
fun privateModule(fn: PrivateKotlinBinder.() -> Unit): Module {
    contract {
        callsInPlace(fn, InvocationKind.EXACTLY_ONCE)
    }
    return object : PrivateModule() {
        override fun configure() {
            fn(binder() as? PrivateKotlinBinder ?: PrivateKotlinBinderImpl(binder()))
        }
    }
}
