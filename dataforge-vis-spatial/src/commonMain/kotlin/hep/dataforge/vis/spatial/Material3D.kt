package hep.dataforge.vis.spatial

import hep.dataforge.meta.*
import hep.dataforge.names.asName
import hep.dataforge.names.plus
import hep.dataforge.vis.common.Colors
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_COLOR_KEY
import hep.dataforge.vis.spatial.Material3D.Companion.MATERIAL_OPACITY_KEY

class Material3D(override val config: Config) : Specific {

    var color by string(key = COLOR_KEY)

    var specularColor by string()

    var opacity by float(1f, key = OPACITY_KEY)

    var wireframe by boolean(false, WIREFRAME_KEY)

    companion object : Specification<Material3D> {
        override fun wrap(config: Config): Material3D = Material3D(config)

        val MATERIAL_KEY = "material".asName()
        internal val COLOR_KEY = "color".asName()
        val MATERIAL_COLOR_KEY = MATERIAL_KEY + COLOR_KEY
        val SPECULAR_COLOR ="specularColor".asName()
        internal val OPACITY_KEY = "opacity".asName()
        val MATERIAL_OPACITY_KEY = MATERIAL_KEY + OPACITY_KEY
        internal val WIREFRAME_KEY = "wireframe".asName()
        val MATERIAL_WIREFRAME_KEY = MATERIAL_KEY + WIREFRAME_KEY

    }
}

fun VisualObject3D.color(rgb: String) {
    setProperty(MATERIAL_COLOR_KEY, rgb)
}

fun VisualObject3D.color(rgb: Int) {
    setProperty(MATERIAL_COLOR_KEY, rgb)
}

fun VisualObject3D.color(r: UByte, g: UByte, b: UByte) = setProperty(
    MATERIAL_COLOR_KEY,
    Colors.rgbToMeta(r, g, b)
)

/**
 * Web colors representation of the color in `#rrggbb` format or HTML name
 */
var VisualObject3D.color: String?
    get() = getProperty(MATERIAL_COLOR_KEY)?.let { Colors.fromMeta(it) }
    set(value) {
        setProperty(MATERIAL_COLOR_KEY, value)
    }

//var VisualObject3D.material: Material3D?
//    get() = getProperty(MATERIAL_KEY).node?.let { Material3D.wrap(it) }
//    set(value) = setProperty(MATERIAL_KEY, value?.config)

fun VisualObject3D.material(builder: Material3D.() -> Unit) {
    val node = config[Material3D.MATERIAL_KEY].node
    if (node != null) {
        Material3D.update(node, builder)
    } else {
        config[Material3D.MATERIAL_KEY] = Material3D.build(builder)
    }
}

var VisualObject3D.opacity: Double?
    get() = getProperty(MATERIAL_OPACITY_KEY).double
    set(value) {
        setProperty(MATERIAL_OPACITY_KEY, value)
    }