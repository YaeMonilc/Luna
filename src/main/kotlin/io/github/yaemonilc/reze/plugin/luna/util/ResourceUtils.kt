package io.github.yaemonilc.reze.plugin.luna.util

import io.github.yaemonilc.reze.plugin.luna.Luna

fun readResourceAsText(
    path: String
) = Luna::class.java.getResource(path).readText()