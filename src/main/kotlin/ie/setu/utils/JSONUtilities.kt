package ie.setu.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.joda.JodaModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinModule

fun jsonObjectMapper(): ObjectMapper
        = ObjectMapper()
    .registerModule(JavaTimeModule())
    .registerModule(JodaModule())
    .registerModule(KotlinModule.Builder().build())
    .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
