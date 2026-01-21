package com.cotarelo.wordle.server
import com.cotarelo.wordle.shared.TestJson
import com.cotarelo.wordle.shared.TestShared

fun main() {
    println("✅ Server OK")
    println(TestJson.encode())
    println(TestShared.greeting("SERVER"))
}
