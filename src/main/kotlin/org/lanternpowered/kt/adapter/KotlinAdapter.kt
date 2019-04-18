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
package org.lanternpowered.kt.adapter

import com.google.inject.AbstractModule
import com.google.inject.Injector
import com.google.inject.Module
import com.google.inject.Provider
import com.google.inject.Scopes
import org.lanternpowered.kt.inject
import org.lanternpowered.kt.inject.InjectablePropertyProvider
import org.lanternpowered.kt.inScope
import org.spongepowered.api.plugin.PluginAdapter
import org.spongepowered.api.plugin.PluginContainer

class KotlinAdapter : PluginAdapter {

    override fun <T : Any> getInjector(
            pluginContainer: PluginContainer, pluginClass: Class<T>, defaultInjector: Injector, pluginModules: List<Module>
    ): Injector {
        val module = object : AbstractModule() {
            override fun configure() {
                install(InjectablePropertyProvider())

                val instance = pluginClass.kotlin.objectInstance
                if (instance != null) {
                    bind(pluginClass).toProvider(InstanceProvider(instance)).inScope(Scopes.SINGLETON)
                } else {
                    bind(pluginClass).inScope(Scopes.SINGLETON)
                }
            }
        }
        return defaultInjector.createChildInjector(pluginModules.toMutableList().apply { add(0, module) })
    }

    /**
     * A instance provider that lazily injects the members of the instance.
     */
    private class InstanceProvider<T>(private val instance: T) : Provider<T> {

        private val injector: Injector by inject()

        override fun get(): T = this.instance.also { this.injector.injectMembers(it) }
    }
}
