package com.argote.tickly.core.platform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform