package com.github.mitallast.ghost.client.persistent

import kotlin.coroutines.experimental.suspendCoroutine
import kotlin.js.Json
import kotlin.js.Promise

external interface IDBException {
    val name: String
    val message: String
    val code: Int
}

// 4.1. The IDBRequest interface

fun <T> IDBRequest<T>.promise(): Promise<T> {
    return Promise({ resolve, reject ->
        onsuccess = { resolve.invoke(result) }
        onerror = {
            console.error("error idb request", it.target.error)
            reject.invoke(Exception(it.toString()))
        }
    })
}

suspend fun <T> IDBRequest<T>.await(): T = suspendCoroutine { cont ->
    onsuccess = { cont.resume(result) }
    onerror = {
        console.error("error idb request", it.target.error)
        cont.resumeWithException(Exception(it.toString()))
    }
}

fun IDBTransaction.promise(): Promise<Unit> {
    return Promise({ resolve, reject ->
        oncomplete = { resolve.invoke(Unit) }
        onerror = {
            console.error("error idb transaction", it.target.error)
            reject.invoke(Exception(it.toString()))
        }
    })
}

suspend fun IDBTransaction.await(): Unit = suspendCoroutine { cont ->
    oncomplete = { cont.resume(Unit) }
    onerror = {
        console.error("error idb transaction", it.target.error)
        cont.resumeWithException(Exception(it.toString()))
    }
}

external interface IDBRequest<out T> {

    val result: T
    val error: IDBException?
    // (IDBObjectStore or IDBIndex or IDBCursor)?
    val source: dynamic
    val transaction: IDBTransaction?
    val readyState: String

    var onerror: (event: dynamic) -> Unit
    var onsuccess: (event: dynamic) -> Unit
}

external interface IDBOpenDBRequest : IDBRequest<IDBDatabase> {
    var onblocked: (event: dynamic) -> Unit
    var onupgradeneeded: (event: IDBVersionChangeEvent) -> Unit

}

object IDBRequestReadyState {
    val pending = "pending"
    val done = "done"
}

// 4.2. Event interfaces

external interface IDBVersionChangeEvent {
    val oldVersion: Int
    val newVersion: Int
}

// 4.3. The IDBFactory interface

external val indexedDB: IDBFactory

external interface IDBFactory {
    fun open(name: String, version: Int = definedExternally): IDBOpenDBRequest
    fun deleteDatabase(name: String): IDBOpenDBRequest
    fun cmp(first: dynamic, second: dynamic): Int
}

// 4.4. The IDBDatabase interface

external interface IDBDatabase {
    val name: String
    val version: Int
    val objectStoreNames: Array<String>

    fun transaction(storeNames: Array<String>, mode: String = definedExternally): IDBTransaction
    fun transaction(storeName: String, mode: String = definedExternally): IDBTransaction
    fun close()
    fun createObjectStore(name: String, options: Json = definedExternally): IDBObjectStore
    fun deleteObjectStore(name: String)

    var onabort: (event: dynamic) -> Unit
    var onclose: (event: dynamic) -> Unit
    var onerror: (event: dynamic) -> Unit
    var onversionchange: (event: dynamic) -> Unit
}

// 4.5. The IDBObjectStore interface

external interface IDBObjectStore {
    val name: String
    val keyPath: dynamic
    val indexNames: Array<String>
    val transaction: IDBTransaction
    val autoIncrement: Boolean

    fun put(value: dynamic, key: dynamic = definedExternally): IDBRequest<dynamic>
    fun add(value: dynamic, key: dynamic = definedExternally): IDBRequest<dynamic>
    fun delete(query: dynamic): IDBRequest<dynamic>
    fun clear(): IDBRequest<dynamic>
    fun <T> get(query: dynamic): IDBRequest<T?>
    fun getKey(query: dynamic): IDBRequest<dynamic>
    fun <T> getAll(query: dynamic = definedExternally, count: Int = definedExternally): IDBRequest<Array<T>>
    fun <T> getAllKeys(query: dynamic = definedExternally, count: Int = definedExternally): IDBRequest<Array<T>>
    fun count(query: dynamic = definedExternally): IDBRequest<dynamic>
    fun openCursor(query: dynamic = definedExternally, direction: String = definedExternally): IDBRequest<IDBCursorWithValue?>
    fun openKeyCursor(query: dynamic = definedExternally, direction: String = definedExternally): IDBRequest<dynamic>
    fun index(name: String): IDBIndex
    fun createIndex(name: String, keyPath: String, options: Json = definedExternally): IDBRequest<dynamic>
    fun createIndex(name: String, keyPath: Array<String>, options: Json = definedExternally): IDBRequest<dynamic>
    fun deleteIndex(name: String)
}

// 4.6. The IDBIndex interface

external interface IDBIndex {
    val name: String
    val objectStore: IDBObjectStore
    val keyPath: dynamic
    val multiEntry: Boolean
    val unique: Boolean

    fun get(query: dynamic): IDBRequest<dynamic>
    fun getKey(query: dynamic): IDBRequest<dynamic>
    fun getAll(query: dynamic = definedExternally, count: Int = definedExternally): IDBRequest<Array<dynamic>>
    fun getAllKeys(query: dynamic = definedExternally, count: Int = definedExternally): IDBRequest<Array<dynamic>>
    fun count(query: dynamic = definedExternally): IDBRequest<dynamic>
    fun openCursor(query: dynamic = definedExternally, direction: String = definedExternally): IDBRequest<IDBCursor?>
    fun openKeyCursor(query: dynamic = definedExternally, direction: String = definedExternally): IDBRequest<dynamic>
}

// 4.7. The IDBKeyRange interface

external class IDBKeyRange {
    val lower: dynamic
    val upper: dynamic
    val lowerOpen: dynamic
    val upperOpen: dynamic

    fun includes(key: dynamic): Boolean

    companion object {
        fun only(value: dynamic): IDBKeyRange
        fun lowerBound(lower: dynamic, open: Boolean = definedExternally): IDBKeyRange
        fun upperBound(upper: dynamic, open: Boolean = definedExternally): IDBKeyRange
        fun bound(lower: dynamic,
                  upper: dynamic,
                  lowerOpen: Boolean = definedExternally,
                  upperOpen: Boolean = definedExternally): IDBKeyRange
    }
}

// 4.8. The IDBCursor interface

external interface IDBCursor {
    // (IDBObjectStore or IDBIndex)
    val source: dynamic
    val direction: String
    val key: dynamic
    val primaryKey: dynamic

    fun advance(count: Int)
    fun `continue`(key: dynamic = definedExternally)
    fun continuePrimaryKey(key: dynamic, primaryKey: dynamic)

    fun update(value: dynamic): IDBRequest<dynamic>
    fun delete(): IDBRequest<dynamic>
}

external interface IDBCursorWithValue : IDBCursor {
    val value: dynamic
}

object IDBCursorDirection {
    val next = "next"
    val nextunique = "nextunique"
    val prev = "prev"
    val prevunique = "prevunique"
}

// 4.9. The IDBTransaction interface

external interface IDBTransaction {
    val objectStoreNames: Array<String>
    val mode: String
    val db: IDBDatabase
    val error: IDBException

    fun objectStore(name: String): IDBObjectStore
    fun abort()

    var onabort: (event: dynamic) -> Unit
    var oncomplete: (event: dynamic) -> Unit
    var onerror: (event: dynamic) -> Unit
}

object IDBTransactionMode {
    val readonly = "readonly"
    val readwrite = "readwrite"
    val versionchange = "versionchange"
}

