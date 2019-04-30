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
import com.google.common.reflect.TypeToken
import com.google.inject.TypeLiteral
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.AnnotatedType
import java.lang.reflect.GenericArrayType
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
import kotlin.reflect.full.createType
import kotlin.reflect.full.starProjectedType
import kotlin.reflect.jvm.javaType

inline fun <reified T> typeToken() = object : TypeToken<T>() {}
inline fun <reified T> typeLiteral() = object : TypeLiteral<T>() {}

inline fun <reified A : Annotation> AnnotatedElement.getAnnotation(): A? = getAnnotation(A::class.java)
inline fun <A : Annotation> AnnotatedElement.getAnnotation(type: KClass<A>): A? = getAnnotation(type.java)

inline val <T> Class<T>.typeToken: TypeToken<T> get() = TypeToken.of(this)
inline val <T> Class<T>.typeLiteral: TypeLiteral<T> get() = TypeLiteral.get(this)

inline val <T : Any> KClass<T>.typeToken: TypeToken<T> get() = TypeToken.of(this.java)
inline val <T : Any> KClass<T>.typeLiteral: TypeLiteral<T> get() = TypeLiteral.get(this.java)

inline val <T> TypeLiteral<T>.typeToken: TypeToken<T> get() = TypeToken.of(this.type) as TypeToken<T>
inline val <T> TypeToken<T>.typeLiteral: TypeLiteral<T> get() = TypeLiteral.get(this.type) as TypeLiteral<T>

inline val <T : Any> TypeLiteral<T>.kotlinClass: KClass<T> get() = (this.rawType as Class<T>).kotlin
inline val <T : Any> TypeToken<T>.kotlinClass: KClass<T> get() = (this.rawType as Class<T>).kotlin

/**
 * Converts the [Type] to a [TypeLiteral].
 */
inline val Type.typeLiteral: TypeLiteral<*> get() = TypeLiteral.get(this)

/**
 * Converts the [Type] to a [TypeToken].
 */
inline val Type.typeToken: TypeToken<*> get() = TypeToken.of(this)

/**
 * Converts the [TypeLiteral] to a [KType].
 */
inline val <T> TypeLiteral<T>.kotlin: TypeLiteral<T> get() = TypeLiteral.get(this.type.kotlin.type) as TypeLiteral<T>

/**
 * Converts the [TypeToken] to a [KType].
 */
inline val <T> TypeToken<T>.kotlin: TypeToken<T> get() = TypeToken.of(this.type.kotlin.type) as TypeToken<T>

/**
 * Converts the [TypeLiteral] to the JVM [Type].
 */
inline val TypeLiteral<*>.jvmType: Type get() = this.type.jvm

/**
 * Converts the [TypeToken] to the JVM [Type].
 */
inline val TypeToken<*>.jvmType: Type get() = this.type.jvm

/**
 * Converts the [TypeLiteral] to a [KType].
 */
inline val TypeLiteral<*>.kotlinType: KType get() = this.type.kotlin

/**
 * Converts the [TypeToken] to a [KType].
 */
inline val TypeToken<*>.kotlinType: KType get() = this.type.kotlin

/**
 * Converts the [TypeLiteral] to the JVM representation.
 */
inline val <T> TypeLiteral<T>.jvm: TypeLiteral<T> get() = TypeLiteral.get(this.type.jvm) as TypeLiteral<T>

/**
 * Converts the [TypeToken] to the JVM representation.
 */
inline val <T> TypeToken<T>.jvm: TypeToken<T> get() = TypeToken.of(this.type.jvm) as TypeToken<T>

/**
 * Gets the raw [Class] from the [Type].
 */
val Type.rawType: Class<*> get() = toRawType(this)

/**
 * Gets the raw [Class] from the [Type].
 */
val KType.rawType: KClass<*> get() = toRawType(this)

/**
 * Converts this [Type] to a JVM based version.
 *
 * @see KType.type
 */
val Type.jvm: Type get() = this.kotlin.jvmType

/**
 * Converts the [Type] to a [KType].
 */
val Type.kotlin: KType get() = toKType(this)

/**
 * Gets the [KClass] for the target [Class].
 */
val <T : Any> Class<T>.kotlin: KClass<T> get() = TypeHelper.getKClass(this)

/**
 * Gets the [TypeLiteral] based on the JVM representation of the [KType].
 *
 * @see KType.javaType
 */
inline val KType.jvmTypeLiteral: TypeLiteral<*> get() = TypeLiteral.get(this.jvmType)

/**
 * Gets the [TypeToken] based on the JVM representation of the [KType].
 *
 * @see KType.javaType
 */
inline val KType.jvmTypeToken: TypeToken<*> get() = TypeToken.of(this.jvmType)

/**
 * Gets the [TypeLiteral] based on the kotlin representation of the [KType].
 *
 * @see KType.type
 */
inline val KType.typeLiteral: TypeLiteral<*> get() = TypeLiteral.get(this.type)

/**
 * Gets the [TypeToken] based on the kotlin representation of the [KType].
 *
 * @see KType.type
 */
inline val KType.typeToken: TypeToken<*> get() = TypeToken.of(this.type)

/**
 * Gets the given [KType] as a [Type]. Unlike the [KType.javaType] this returns
 * the [Type] literally translated from the [KType], and not based on the actual [Type]
 * used in the JVM.
 *
 * For example a return type in kotlin would be [Unit], but when translated to the JVM
 * type it would turn into `void`. This also affects places where inline class types
 * are inlined.
 *
 * @see KType.javaType
 */
val KType.type: Type get() = toJavaType(this)

/**
 * Gets the JVM representation of the given [KType].
 *
 * @see KType.type
 */
val KType.jvmType: Type get() = this.javaType

private fun toRawType(type: Type): Class<*> {
    return when(type) {
        is Class<*> -> type
        is ParameterizedType -> type.rawType.rawType
        is GenericArrayType -> TypeHelper.getArrayClass(type.genericComponentType.rawType)
        is TypeVariable<*> -> type.bounds.firstOrNull()?.rawType ?: Any::class.java
        is WildcardType -> (type.upperBounds.firstOrNull() ?: type.lowerBounds.firstOrNull())?.rawType ?: Any::class.java
        else -> Any::class.java // Unknown type
    }
}

private fun toRawType(kType: KType): KClass<*> {
    return when(val classifier = kType.classifier) {
        null -> Any::class
        is KClass<*> -> classifier
        is KTypeParameter -> classifier.upperBounds.firstOrNull()?.rawType ?: Any::class
        else -> throw UnsupportedOperationException()
    }
}

private fun toKType(type: Type): KType {
    return when (type) {
        is KTypeHolder -> type.kType
        is Class<*> -> type.kotlin.starProjectedType
        is ParameterizedType -> {
            val typeProjections = type.actualTypeArguments.map { toKTypeProjection(it) }
            (type.rawType as Class<*>).kotlin.createType(typeProjections)
        }
        is TypeVariable<*> -> {
            val bounds = type.bounds.map { toKType(it) }.toList()
            val annotations = type.annotations.toList()
            return KTypeParameterImpl(false, type.name, bounds, KVariance.OUT).createType(annotations = annotations)
        }
        is WildcardType -> {
            val lower = type.lowerBounds
            return if (lower.isNotEmpty()) {
                toKType(lower[0])
            } else {
                val upper = type.upperBounds
                toKType(upper.firstOrNull() ?: Any::class.java)
            }
        }
        is GenericArrayType -> {
            val typeProjection = toKTypeProjection(type.genericComponentType)
            Array<Any>::class.createType(listOf(typeProjection))
        }
        else -> throw UnsupportedOperationException("Unknown type: $type")
    }
}

/**
 * KTypeParameter implementation.
 */
private class KTypeParameterImpl(
        override val isReified: Boolean,
        override val name: String,
        override val upperBounds: List<KType>,
        override val variance: KVariance
) : KTypeParameter

private fun toKTypeProjection(type: Type): KTypeProjection {
    return when(type) {
        is Class<*>, is ParameterizedType, is GenericArrayType, is KTypeHolder, is TypeVariable<*> -> KTypeProjection.invariant(toKType(type))
        is WildcardType -> {
            val lower = type.lowerBounds
            return if (lower.isNotEmpty()) {
                KTypeProjection.contravariant(toKType(lower[0]))
            } else {
                val upper = type.upperBounds
                if (upper.isNotEmpty()) {
                    KTypeProjection.covariant(toKType(upper[0]))
                } else {
                    KTypeProjection.STAR
                }
            }
        }
        else -> throw UnsupportedOperationException("Unknown type: $type")
    }
}

/**
 * Represents the root type, which also represents the generic declaration if there are any.
 */
private class RootKType(val parentKType: KType) {

    val genericDeclaration by lazy { GenericDeclarationImpl(this.parentKType.annotations.toTypedArray()) }
}

/**
 * Represents a interface to convert back from [Type] to [KType].
 */
private interface KTypeHolder : Type {

    /**
     * The original [KType].
     */
    val kType: KType
}

/**
 * Converts the given [KType] into a [Type]. Unlike the [KType.javaType] this returns
 * the [Type] literally translated from the [KType], and not based in the actual [Type]
 * used in the JVM.
 */
private fun toJavaType(kType: KType, rootKType: RootKType? = null): Type {
    return when(val classifier = kType.classifier) {
        null -> UnknownType(kType)
        is KTypeParameter -> {
            // If there's no parent type, fallback to a dummy declaration, shouldn't happen in most cases
            val genericDeclaration = rootKType?.genericDeclaration ?: GenericDeclarationImpl(emptyArray())
            val type = toJavaType(classifier, genericDeclaration, kType, rootKType)
            genericDeclaration.typeVariables.add(type)
            type
        }
        is KClass<*> -> {
            // Attempt to extract the java type from the classifier
            val javaClass = classifier.java
            if (javaClass.isPrimitive) {
                // Primitive classes inside generics aren't supported, only as root type
                return if (rootKType == null) javaClass else Primitives.wrap(javaClass)
            }
            // Well now if there's any parameter types we need to consider
            val arguments = kType.arguments
            if (javaClass.isArray) {
                val componentType = arguments.getOrNull(0)?.type
                if (componentType != null) {
                    val rawComponentType = componentType.javaType.rawType
                    if (rawComponentType.isPrimitive) { // Found a primitive array, just return the array class
                        return javaClass
                    }
                    // Wrap in a GenericArrayType, even if there aren't any generics, this is to store the KType
                    return GenericArrayTypeImpl(toJavaType(componentType, rootKType), componentType)
                }
                // No component type?
                return TypeHelper.getArrayClass(Any::class.java)
            }
            // Fast return
            if (arguments.isEmpty()) return javaClass
            val nextRootType = rootKType ?: RootKType(kType)
            val argumentTypes = arguments.map { projection -> toJavaType(projection, nextRootType) }.toTypedArray()
            return ParameterizedTypeImpl(javaClass, argumentTypes, kType)
        }
        else -> throw UnsupportedOperationException()
    }
}

/**
 * Represents a [WildcardType] of a star projection.
 */
private val STAR_PROJECTION = WildcardTypeImpl(emptyArray(), emptyArray())

/**
 * Converts the given [KTypeProjection] into a [Type].
 */
private fun toJavaType(typeProjection: KTypeProjection, rootKType: RootKType?): Type {
    val kType = typeProjection.type ?: return STAR_PROJECTION // Null represents star projection
    val type = toJavaType(kType, rootKType)
    return when (typeProjection.variance!!) {
        KVariance.INVARIANT -> type
        KVariance.IN -> KTypeWildcardTypeImpl(arrayOf(type), emptyArray(), kType)
        KVariance.OUT -> KTypeWildcardTypeImpl(emptyArray(), arrayOf(type), kType)
        else -> throw UnsupportedOperationException()
    }
}

/**
 * Converts the given [KTypeParameter] into a [Type].
 */
private fun toJavaType(typeParameter: KTypeParameter, genericDeclaration: GenericDeclaration, kType: KType, rootKType: RootKType?): TypeVariable<*> {
    val annotatedBounds = typeParameter.upperBounds
            .map { type -> AnnotatedTypeImpl(toJavaType(type, rootKType), type.annotations.toTypedArray()) }
            .toTypedArray<AnnotatedType>()
    val bounds = annotatedBounds.map { type -> type.type }.toTypedArray()
    return TypeVariableImpl(typeParameter.name, kType.annotations.toTypedArray(), bounds, annotatedBounds, genericDeclaration, kType)
}

/**
 * Converts the given [Type] to a [String].
 */
private fun typeToString(type: Type): String {
    return if (type is Class<*>) {
        type.kotlin.qualifiedName ?: type.name
    } else {
        type.toString()
    }
}

/**
 * A unknown [Type], one that cannot be represented as a default one.
 */
private class UnknownType(override val kType: KType) : KTypeHolder

/**
 * A implementation of [AnnotatedType].
 */
private class AnnotatedTypeImpl(private val type: Type, annotations: Array<Annotation>) : AnnotatedElementImpl(annotations), AnnotatedType {

    override fun getType() = this.type
    override fun toString(): String = typeToString(this.type)
}

private class GenericArrayTypeImpl(private val componentType: Type, override val kType: KType) : GenericArrayType, KTypeHolder {

    override fun getGenericComponentType() = this.componentType
    override fun toString() = "kotlin.Array<${typeToString(this.componentType)}>"
}

/**
 * A implementation of [ParameterizedType].
 */
private class ParameterizedTypeImpl(
        private val javaClass: Class<*>, private val argumentTypes: Array<Type>, override val kType: KType
) : ParameterizedType, KTypeHolder {

    override fun getRawType(): Type = this.javaClass
    override fun getOwnerType(): Type? = this.javaClass.enclosingClass
    override fun getActualTypeArguments() = this.argumentTypes.copyOf()
    override fun toString() = "${this.javaClass.name}<${this.argumentTypes.joinToString(",") { typeToString(it) }}>"
}

/**
 * A implementation of [WildcardType].
 */
private open class WildcardTypeImpl(private val lower: Array<Type>, private val upper: Array<Type>) : WildcardType {

    override fun getLowerBounds() = this.lower.copyOf()
    override fun getUpperBounds() = this.upper.copyOf()
}

/**
 * A implementation of [WildcardType].
 */
private class KTypeWildcardTypeImpl(lower: Array<Type>, upper: Array<Type>, override val kType: KType) : WildcardTypeImpl(lower, upper), KTypeHolder

/**
 * A implementation of [TypeVariable].
 */
private class TypeVariableImpl<D : GenericDeclaration>(
        private val name: String,
        annotations: Array<Annotation>,
        private val bounds: Array<Type>,
        private val annotatedBounds: Array<AnnotatedType>,
        private val genericDeclaration: D,
        override val kType: KType
) : AnnotatedElementImpl(annotations), TypeVariable<D>, KTypeHolder {

    override fun getName() = this.name
    override fun getAnnotatedBounds() = this.annotatedBounds.copyOf()
    override fun getBounds() = this.bounds.copyOf()
    override fun getGenericDeclaration() = this.genericDeclaration
    override fun toString() = this.name
}

/**
 * A implementation of [AnnotatedElement].
 */
private open class AnnotatedElementImpl(private val annotations: Array<Annotation>) : AnnotatedElement {

    override fun getAnnotations() = this.annotations.copyOf()
    override fun getDeclaredAnnotations() = this.annotations.copyOf()
    override fun <T : Annotation> getAnnotation(annotationClass: Class<T>) = this.annotations.find(annotationClass::isInstance) as T?
}

/**
 * A implementation of [GenericDeclaration].
 */
private class GenericDeclarationImpl(annotations: Array<Annotation>) : AnnotatedElementImpl(annotations), GenericDeclaration {

    internal var typeVariables = mutableListOf<TypeVariable<*>>()
    override fun getTypeParameters() = this.typeVariables.toTypedArray()
}
