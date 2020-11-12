@file:UseSerializers(MetaSerializer::class)

package hep.dataforge.vision

import hep.dataforge.meta.*
import hep.dataforge.names.Name
import hep.dataforge.names.asName
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * A container for styles
 */
@Serializable(StyleSheet.Companion::class)
public class StyleSheet private constructor(private val styleMap: MutableMap<String, Meta>) {
    @Transient
    internal var owner: Vision? = null

    public constructor(owner: Vision) : this(LinkedHashMap()) {
        this.owner = owner
    }

    public val items: Map<String, Meta> get() = styleMap


    private fun Vision.styleChanged(key: String, oldStyle: Meta?, newStyle: Meta?) {
        if (styles.contains(key)) {
            //TODO optimize set concatenation
            val tokens: Collection<Name> =
                ((oldStyle?.items?.keys ?: emptySet()) + (newStyle?.items?.keys ?: emptySet()))
                    .map { it.asName() }
            tokens.forEach { parent?.propertyChanged(it) }
        }
        if (this is VisionGroup) {
            for (obj in this) {
                obj.styleChanged(key, oldStyle, newStyle)
            }
        }
    }

    public operator fun get(key: String): Meta? {
        return styleMap[key] ?: owner?.parent?.styleSheet?.get(key)
    }

    /**
     * Define a style without notifying owner
     */
    public fun define(key: String, style: Meta?) {
        if (style == null) {
            styleMap.remove(key)
        } else {
            styleMap[key] = style
        }
    }

    /**
     * Set or clear the style
     */
    public operator fun set(key: String, style: Meta?) {
        val oldStyle = styleMap[key]
        define(key, style)
        owner?.styleChanged(key, oldStyle, style)
    }

    /**
     * Create and set a style
     */
    public operator fun set(key: String, builder: MetaBuilder.() -> Unit) {
        val newStyle = get(key)?.edit(builder) ?: Meta(builder)
        set(key, newStyle.seal())
    }

    public fun update(key: String, meta: Meta) {
        val existing = get(key)
        set(key, existing?.edit { this.update(meta) } ?: meta)
    }

    public fun update(other: StyleSheet) {
        other.items.forEach { (key, value) ->
            update(key, value)
        }
    }

    public companion object : KSerializer<StyleSheet> {
        private val mapSerializer = MapSerializer(String.serializer(), MetaSerializer)
        override val descriptor: SerialDescriptor get() = mapSerializer.descriptor


        override fun deserialize(decoder: Decoder): StyleSheet {
            val map = mapSerializer.deserialize(decoder)
            return StyleSheet(map as? MutableMap<String, Meta> ?: LinkedHashMap(map))
        }

        override fun serialize(encoder: Encoder, value: StyleSheet) {
            mapSerializer.serialize(encoder, value.items)
        }
    }
}

/**
 * Add style name to the list of styles to be resolved later. The style with given name does not necessary exist at the moment.
 */
public fun Vision.useStyle(name: String) {
    styles = (properties[Vision.STYLE_KEY]?.stringList ?: emptyList()) + name
}

/**
 * Resolve an item in all style layers
 */
public fun Vision.getStyleItems(name: Name): Sequence<MetaItem<*>> {
    return styles.asSequence().map {
        resolveStyle(it)
    }.map {
        it[name]
    }.filterNotNull()
}

/**
 * Collect all styles for this object in a single laminate
 */
public val Vision.allStyles: Laminate get() = Laminate(styles.mapNotNull(::resolveStyle))