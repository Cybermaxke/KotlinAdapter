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
import com.google.inject.Module
import com.google.inject.Scopes
import com.google.inject.util.Modules
import org.lanternpowered.kt.inject.InjectionPointModule
import org.lanternpowered.kt.inject.KotlinInjectionModule
import org.lanternpowered.kt.inject.inScope
import org.spongepowered.api.plugin.PluginAdapter
import org.spongepowered.api.plugin.PluginContainer

class KotlinAdapter : PluginAdapter {

    override fun createGlobalModule(defaultModule: Module): Module
            = Modules.combine(Modules.override(defaultModule).with(InjectionPointModule()), KotlinInjectionModule())

    override fun <T : Any> createPluginModule(pluginContainer: PluginContainer, pluginClass: Class<T>, defaultModule: Module): Module {
        return object : AbstractModule() {
            override fun configure() {
                val instance = pluginClass.kotlin.objectInstance
                if (instance != null) {
                    bind(pluginClass).toInstance(instance)
                } else {
                    bind(pluginClass).inScope(Scopes.SINGLETON)
                }
                install(defaultModule)
            }
        }
    }
}
