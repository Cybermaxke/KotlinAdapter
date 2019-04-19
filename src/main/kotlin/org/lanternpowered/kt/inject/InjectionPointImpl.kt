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

import com.google.common.base.MoreObjects
import com.google.common.reflect.TypeToken
import com.google.inject.Key
import org.spongepowered.api.inject.InjectionPoint
import java.util.Arrays

internal open class InjectionPointImpl(
        private val source: TypeToken<*>,
        private val type: TypeToken<*>,
        private val annotations: Array<Annotation>
) : InjectionPoint {

    override fun getSource(): TypeToken<*> = this.source
    override fun getType(): TypeToken<*> = this.type

    override fun <A : Annotation> getAnnotation(annotationClass: Class<A>): A? =
            this.annotations.firstOrNull { annotationClass.isInstance(it) } as A?

    override fun getAnnotations() = this.annotations.copyOf()
    override fun getDeclaredAnnotations() = getAnnotations()

    override fun toString(): String {
        return MoreObjects.toStringHelper("InjectionPoint")
                .add("source", this.source)
                .add("type", this.type)
                .add("annotations", Arrays.toString(this.annotations))
                .toString()
    }

    internal class KProperty(source: TypeToken<*>, type: TypeToken<*>, annotations: Array<Annotation>, internal val key: Key<*>)
        : InjectionPointImpl(source, type, annotations)
}
