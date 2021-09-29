package de.tum.www1.orion.util

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.google.gson.*
import java.lang.reflect.Type
import java.time.ZonedDateTime

class ZonedDateTimeDeserializer : JsonDeserializer<ZonedDateTime> {
    override fun deserialize(json: JsonElement?, typeOfT: Type?, context: JsonDeserializationContext?): ZonedDateTime {
        return ZonedDateTime.parse(json?.asString)
    }
}

/**
 * Helper class providing access to the gson object required for parsing json
 */
object JsonUtils {
    fun gson(): Gson = GsonBuilder()
        .registerTypeAdapter(ZonedDateTime::class.java, ZonedDateTimeDeserializer())
        .create()

    fun mapper(): ObjectMapper = ObjectMapper().registerKotlinModule()
}
