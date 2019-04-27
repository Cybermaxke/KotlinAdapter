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
package org.lanternpowered.kt.plugin

import org.lanternpowered.kt.adapter.KotlinAdapter
import org.lanternpowered.kt.inject.inject
import org.slf4j.Logger
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer

@Plugin(id = "kotlin_adapter_test", name = "Kotlin Adapter Test", adapter = KotlinAdapter::class, injectionModules = [ TestModule::class ])
object TestPlugin {

    private val logger: Logger by inject()
    private val pluginContainer: PluginContainer by inject()

    //private val customTestString: TestCustomString by inject()
    private val testObject: TestObject? by inject()
    @TestBindingAnnotation private val pluginName: String by inject()
    @TestNamed("MyTest") private val testName: String by inject()

    @Listener
    fun onInit(event: GameInitializationEvent) {
        this.logger.info("Successfully loaded the Kotlin Adapter test plugin. -> $pluginName")

        check(this == TestPlugin) { "Plugin instance mismatch" }
        check(this.pluginName == this.pluginContainer.name) { "Custom injection mismatch" }
        check(this.testName == "MyTest") { "Custom injection mismatch" }
        //check(this.customTestString.value == "Custom Test String") { "Custom injection mismatch" }
        //check(this.testObject.customTestString.value == "Custom Test String") { "Custom injection mismatch" }
    }
}
