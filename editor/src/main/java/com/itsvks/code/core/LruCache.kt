package com.itsvks.code.core

class LruCache<K, V>(private val maxSize: Int) : LinkedHashMap<K, V>(maxSize, 0.75f, true) {
    override fun removeEldestEntry(eldest: Map.Entry<K, V>) = size > maxSize
}
