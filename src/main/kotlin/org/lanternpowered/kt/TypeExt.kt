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
@file:Suppress("UNCHECKED_CAST", "NOTHING_TO_INLINE")

package org.lanternpowered.kt

import com.google.common.reflect.TypeParameter
import com.google.common.reflect.TypeToken
import com.google.inject.TypeLiteral
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Type
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.jvm.javaType

inline fun <reified T> typeToken() = object : TypeToken<T>() {}
inline fun <reified T> typeLiteral() = object : TypeLiteral<T>() {}
inline fun <reified T> typeParameter() = object : TypeParameter<T>() {}

inline val Type.typeToken: TypeToken<*> get() = TypeToken.of(this)
inline val Type.typeLiteral: TypeLiteral<*> get() = TypeLiteral.get(this)

inline val KType.typeToken: TypeToken<*> get() = TypeToken.of(this.javaType)
inline val KType.typeLiteral: TypeLiteral<*> get() = TypeLiteral.get(this.javaType)

inline val <T> Class<T>.typeToken: TypeToken<T> get() = TypeToken.of(this)
inline val <T> Class<T>.typeLiteral: TypeLiteral<T> get() = TypeLiteral.get(this)

inline val <T : Any> KClass<T>.typeToken: TypeToken<T> get() = TypeToken.of(this.java)
inline val <T : Any> KClass<T>.typeLiteral: TypeLiteral<T> get() = TypeLiteral.get(this.java)

inline val <T> TypeLiteral<T>.typeToken: TypeToken<T> get() = TypeToken.of(this.type) as TypeToken<T>
inline val <T> TypeToken<T>.typeLiteral: TypeLiteral<T> get() = TypeLiteral.get(this.type) as TypeLiteral<T>

inline fun <reified A : Annotation> AnnotatedElement.getAnnotation(): A? = getAnnotation(A::class.java)
inline fun <A : Annotation> AnnotatedElement.getAnnotation(type: KClass<A>): A? = getAnnotation(type.java)
