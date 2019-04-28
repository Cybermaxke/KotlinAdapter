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

import com.google.common.primitives.Primitives
import com.google.common.reflect.TypeParameter
import com.google.common.reflect.TypeToken
import com.google.inject.TypeLiteral
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.AnnotatedType
import java.lang.reflect.GenericDeclaration
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.KTypeParameter
import kotlin.reflect.KTypeProjection
import kotlin.reflect.KVariance
import kotlin.reflect.jvm.internal.impl.types.TypeProjection
import kotlin.reflect.jvm.javaType

inline fun <reified T> typeToken() = object : TypeToken<T>() {}
inline fun <reified T> typeLiteral() = object : TypeLiteral<T>() {}
inline fun <reified T> typeParameter() = object : TypeParameter<T>() {}

inline val Type.typeToken: TypeToken<*> get() = TypeToken.of(this)
inline val Type.typeLiteral: TypeLiteral<*> get() = TypeLiteral.get(this)

inline val <T> Class<T>.typeToken: TypeToken<T> get() = TypeToken.of(this)
inline val <T> Class<T>.typeLiteral: TypeLiteral<T> get() = TypeLiteral.get(this)

inline val <T : Any> KClass<T>.typeToken: TypeToken<T> get() = TypeToken.of(this.java)
inline val <T : Any> KClass<T>.typeLiteral: TypeLiteral<T> get() = TypeLiteral.get(this.java)

inline val <T> TypeLiteral<T>.typeToken: TypeToken<T> get() = TypeToken.of(this.type) as TypeToken<T>
inline val <T> TypeToken<T>.typeLiteral: TypeLiteral<T> get() = TypeLiteral.get(this.type) as TypeLiteral<T>

inline val <T : Any> TypeLiteral<T>.kotlinClass: KClass<T> get() = (this.rawType as Class<T>).kotlin
inline val <T : Any> TypeToken<T>.kotlinClass: KClass<T> get() = (this.rawType as Class<T>).kotlin

inline fun <reified A : Annotation> AnnotatedElement.getAnnotation(): A? = getAnnotation(A::class.java)
inline fun <A : Annotation> AnnotatedElement.getAnnotation(type: KClass<A>): A? = getAnnotation(type.java)

val KType.javaTypeLiteral: TypeLiteral<*> get() = TypeLiteral.get(this.javaType)
val KType.javaTypeToken: TypeToken<*> get() = TypeToken.of(this.javaType)

val KType.typeLiteral: TypeLiteral<*> get() = TypeLiteral.get(toJavaType(this))
val KType.typeToken: TypeToken<*> get() = TypeToken.of(toJavaType(this))

/**
 * Converts the given [KType] into a kotlin based java [Type]. It's not the literal
 * representation. E.g. a inline class type will still be retained when it's inlined,
 * unlike [KType.javaType] which can return its underlying inlined type.
 */
private fun toJavaType(kType: KType, root: Boolean = true): Type {
    return when(kType) {
        is KClass<*> -> if (root) kType.java else kType.javaObjectType // Primitive classes inside generics aren't supported, only as root type
        else -> {
            // Inline class types can be stripped in some occasions
            // when calling javaType. Because the e.g. a field type is
            // as a compiled state inlined as a different type.
            val classifier = kType.classifier
            // Attempt to extract the java type from the classifier
            val javaClass = (classifier as? KClass<*>)?.java ?: run {
                // Try to use the toString of the KType as fallback
                val toString = kType.toString()
                val index = toString.indexOf('<') // Strip the generic signature
                Class.forName(if (index == -1) toString else toString.substring(0, index))
            }
            if (javaClass.isPrimitive) {
                return if (root) javaClass else Primitives.wrap(javaClass) // Primitive classes inside generics aren't supported, only as root type
            }
            // Well now if there's any parameter types we need to consider
            val arguments = kType.arguments
            // Fast return
            if (arguments.isEmpty()) {
                return javaClass
            }
            val argumentTypes = arguments.map { projection -> toJavaType(projection) }.toTypedArray()
            return ParameterizedTypeImpl(javaClass, argumentTypes)
        }
    }
}

/**
 * Represents a [WildcardType] of a star projection.
 */
private val StarProjection = WildcardTypeImpl(emptyArray(), emptyArray())

/**
 * Converts the given [KTypeProjection] into a java [Type].
 */
private fun toJavaType(typeProjection: KTypeProjection): Type {
    val kType = typeProjection.type ?: return StarProjection // Null represents star projection
    val type = toJavaType(kType, false)
    return when (typeProjection.variance!!) {
        KVariance.INVARIANT -> type
        KVariance.IN -> WildcardTypeImpl(arrayOf(type), emptyArray())
        KVariance.OUT -> WildcardTypeImpl(emptyArray(), arrayOf(type))
        else -> throw UnsupportedOperationException()
    }
}

/**
 * Converts the given [KTypeParameter] into a java [Type].
 */
private fun toJavaType(typeParameter: KTypeParameter, genericDeclaration: GenericDeclaration, annotations: Array<Annotation>): Type {
    val annotatedBounds = typeParameter.upperBounds
            .map { kType -> AnnotatedTypeImpl(toJavaType(kType, false), kType.annotations.toTypedArray()) }
            .toTypedArray<AnnotatedType>()
    val bounds = annotatedBounds.map { type -> type.type }.toTypedArray()
    return TypeVariableImpl(typeParameter.name, annotations, bounds, annotatedBounds, genericDeclaration)
}

private fun typeToString(type: Type) = if (type is Class<*>) type.name else type.toString()

private class AnnotatedTypeImpl(private val type: Type, annotations: Array<Annotation>) : AnnotatedElementImpl(annotations), AnnotatedType {

    override fun getType() = this.type
    override fun toString() = typeToString(this.type)
}

private class ParameterizedTypeImpl(private val javaClass: Class<*>, private val argumentTypes: Array<Type>) : ParameterizedType {

    override fun getRawType(): Type = this.javaClass
    override fun getOwnerType(): Type = this.javaClass.enclosingClass
    override fun getActualTypeArguments() = this.argumentTypes.copyOf()
    override fun toString() = "${this.javaClass.name}<${this.argumentTypes.joinToString(", ") { typeToString(it) }}>"
}

private class WildcardTypeImpl(private val lower: Array<Type>, private val upper: Array<Type>) : WildcardType {

    override fun getLowerBounds() = this.lower.copyOf()
    override fun getUpperBounds() = this.upper.copyOf()
}

private class TypeVariableImpl<D : GenericDeclaration>(
        private val name: String,
        annotations: Array<Annotation>,
        private val bounds: Array<Type>,
        private val annotatedBounds: Array<AnnotatedType>,
        private val genericDeclaration: D
) : AnnotatedElementImpl(annotations), TypeVariable<D> {

    override fun getName() = this.name
    override fun getAnnotatedBounds() = this.annotatedBounds.copyOf()
    override fun getBounds() = this.bounds.copyOf()
    override fun getGenericDeclaration() = this.genericDeclaration
    override fun toString() = this.name
}

private open class AnnotatedElementImpl(private val annotations: Array<Annotation>) : AnnotatedElement {

    override fun getAnnotations() = this.annotations.copyOf()
    override fun getDeclaredAnnotations() = this.annotations.copyOf()
    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>) = this.annotations.find(annotationClass::isInstance) as T?
}

private class GenericDeclarationImpl(
        annotations: Array<Annotation>
) : AnnotatedElementImpl(annotations), GenericDeclaration {

    internal var typeVariables: Array<TypeVariable<*>>? = null

    override fun getTypeParameters() = this.typeVariables?.copyOf() ?: emptyArray()
}
