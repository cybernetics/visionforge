package hep.dataforge.vis.spatial.demo

import hep.dataforge.context.ContextBuilder
import hep.dataforge.context.Global
import hep.dataforge.js.Application
import hep.dataforge.js.startApplication
import hep.dataforge.vis.spatial.three.MeshThreeFactory.Companion.EDGES_ENABLED_KEY
import hep.dataforge.vis.spatial.three.MeshThreeFactory.Companion.WIREFRAME_ENABLED_KEY
import hep.dataforge.vis.spatial.three.ThreePlugin
import hep.dataforge.vis.spatial.x
import hep.dataforge.vis.spatial.y
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.browser.document
import kotlin.random.Random

private class ThreeDemoApp : Application {

    override fun start(state: Map<String, Any>) {

        val element = document.getElementById("canvas") ?: error("Element with id 'canvas' not found on page")

        ThreeDemoGrid(element).run {
            showcase()
            showcaseCSG()
            demo("dynamicBox", "Dancing boxes") {
                val boxes = (-10..10).flatMap { i ->
                    (-10..10).map { j ->
                        varBox(10, 10, 0, name = "cell_${i}_${j}") {
                            x = i * 10
                            y = j * 10
                            value = 128
                            setProperty(EDGES_ENABLED_KEY, false)
                            setProperty(WIREFRAME_ENABLED_KEY, false)
                        }
                    }
                }
                GlobalScope.launch {
                    while (isActive) {
                        delay(500)
                        boxes.forEach { box ->
                            box.value = (box.value + Random.nextInt(-15, 15)).coerceIn(0..255)
                        }
                    }
                }
            }
        }


    }

    override fun dispose() = emptyMap<String, Any>()//mapOf("lines" put presenter.dispose())
}

fun main() {
    startApplication(::ThreeDemoApp)
}