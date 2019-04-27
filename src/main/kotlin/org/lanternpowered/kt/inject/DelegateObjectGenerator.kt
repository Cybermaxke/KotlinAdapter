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

import com.google.common.collect.MapMaker
import org.lanternpowered.lmbda.kt.createLambda
import org.lanternpowered.lmbda.kt.defineClass
import org.lanternpowered.lmbda.kt.privateLookupIn
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Label
import org.objectweb.asm.Opcodes.ACC_PRIVATE
import org.objectweb.asm.Opcodes.ACC_PUBLIC
import org.objectweb.asm.Opcodes.ACC_SUPER
import org.objectweb.asm.Opcodes.ALOAD
import org.objectweb.asm.Opcodes.ARETURN
import org.objectweb.asm.Opcodes.ASTORE
import org.objectweb.asm.Opcodes.ATHROW
import org.objectweb.asm.Opcodes.CHECKCAST
import org.objectweb.asm.Opcodes.DUP
import org.objectweb.asm.Opcodes.F_APPEND
import org.objectweb.asm.Opcodes.GETFIELD
import org.objectweb.asm.Opcodes.IFNULL
import org.objectweb.asm.Opcodes.ILOAD
import org.objectweb.asm.Opcodes.INVOKEINTERFACE
import org.objectweb.asm.Opcodes.INVOKESPECIAL
import org.objectweb.asm.Opcodes.IRETURN
import org.objectweb.asm.Opcodes.NEW
import org.objectweb.asm.Opcodes.PUTFIELD
import org.objectweb.asm.Opcodes.RETURN
import org.objectweb.asm.Opcodes.V1_8
import org.objectweb.asm.Type
import java.lang.invoke.MethodHandles
import java.lang.invoke.MethodType
import java.util.concurrent.atomic.AtomicInteger

@PublishedApi internal object DelegateObjectGenerator {

    private val counter = AtomicInteger()
    private val delegateSuppliers = MapMaker().weakKeys().concurrencyLevel(4).makeMap<Class<*>, () -> Any>()

    fun <T> generateDelegateObject(interfaceToDelegate: Class<T>): T {
        val supplier = this.delegateSuppliers.computeIfAbsent(interfaceToDelegate) {
            val delegateClass = generateDelegateClass(interfaceToDelegate)
            val methodHandle = MethodHandles.publicLookup().findConstructor(delegateClass, MethodType.methodType(Void.TYPE))
            methodHandle.createLambda()
        }
        return supplier() as T
    }

    private fun generateDelegateClass(interfaceToDelegate: Class<*>): Class<*> {
        val cw = ClassWriter(0)

        val delegateInterfaceName = Type.getInternalName(interfaceToDelegate)
        val delegateInterfaceDescriptor = Type.getDescriptor(interfaceToDelegate)

        val injectedDelegateName = Type.getInternalName(InjectedDelegate::class.java)

        val className = "$delegateInterfaceName$\$DelegateImpl$${counter.incrementAndGet()}"
        val delegateFieldName = "delegate"
        val checkInjectedMethodName = "checkInjected"

        cw.visit(V1_8, ACC_PUBLIC + ACC_SUPER, className, null, "java/lang/Object",
                arrayOf(delegateInterfaceName, injectedDelegateName))

        cw.visitField(ACC_PRIVATE, delegateFieldName, delegateInterfaceDescriptor, null, null).visitEnd()

        cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null).run {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false)
            visitInsn(RETURN)
            visitMaxs(1, 1)
            visitEnd()
        }

        cw.visitMethod(ACC_PUBLIC, "injectDelegateObject", "(Ljava/lang/Object;)V", null, null).run {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitVarInsn(ALOAD, 1)
            visitTypeInsn(CHECKCAST, delegateInterfaceName)
            visitFieldInsn(PUTFIELD, className, delegateFieldName, delegateInterfaceDescriptor)
            visitInsn(RETURN)
            visitMaxs(2, 2)
            visitEnd()
        }

        cw.visitMethod(ACC_PRIVATE, checkInjectedMethodName, "()$delegateInterfaceDescriptor", null, null).run {
            visitCode()
            visitVarInsn(ALOAD, 0)
            visitFieldInsn(GETFIELD, className, delegateFieldName, delegateInterfaceDescriptor)
            visitVarInsn(ASTORE, 1)
            visitVarInsn(ALOAD, 1)
            val jumpLabel = Label()
            visitJumpInsn(IFNULL, jumpLabel)
            visitVarInsn(ALOAD, 1)
            visitInsn(ARETURN)
            visitLabel(jumpLabel)
            visitFrame(F_APPEND, 1, arrayOf<Any>(delegateInterfaceName), 0, null)
            visitTypeInsn(NEW, "java/lang/IllegalStateException")
            visitInsn(DUP)
            visitLdcInsn("The delegate for interface '${interfaceToDelegate.simpleName}' is not yet injected.")
            visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V", false)
            visitInsn(ATHROW)
            visitMaxs(3, 2)
            visitEnd()
        }

        for (method in interfaceToDelegate.methods) {
            val descriptor = Type.getMethodDescriptor(method)
            cw.visitMethod(ACC_PUBLIC, method.name, descriptor, null, null).run {
                visitCode()
                visitVarInsn(ALOAD, 0)
                visitMethodInsn(INVOKESPECIAL, className, checkInjectedMethodName, "()$delegateInterfaceDescriptor", false)
                val maxs = method.parameterCount + 1
                method.parameterTypes.forEachIndexed { index, parameter ->
                    visitVarInsn(Type.getType(parameter).getOpcode(ILOAD), index + 1)
                }
                visitMethodInsn(INVOKEINTERFACE, delegateInterfaceName, method.name, descriptor, true)
                visitInsn(Type.getType(method.returnType).getOpcode(IRETURN))
                visitMaxs(maxs, maxs)
                visitEnd()
            }
        }

        cw.visitEnd()

        val lookup = MethodHandles.lookup().privateLookupIn(interfaceToDelegate)
        return lookup.defineClass(cw.toByteArray())
    }

    /*
    public class TestDelegateImpl implements ITestInterface, InjectedDelegate {

        // Store the delegate
        private ITestInterface delegate;

        // Inject the delegate object with guice
        @Override
        public void injectDelegateObject(Object delegate) {
            this.delegate = (ITestInterface) delegate;
        }

        // Utility method to validate that the delegate is injected
        private ITestInterface checkInjected() {
            final ITestInterface delegate = this.delegate;
            if (delegate != null) {
                return delegate;
            }
            throw new IllegalStateException("The delegate interface ITestInterface is not yet injected.");
        }

        // Delegate all interface methods

        @Override
        public String getTest() {
            return checkInjected().getTest();
        }
    }
    */
}
