package com.github.mitallast.ghost.persistent

import com.github.mitallast.ghost.common.file.FileService
import org.apache.logging.log4j.LogManager
import org.rocksdb.*
import javax.inject.Inject

class PersistentService @Inject constructor(private val fileService: FileService) {

    private val logger = LogManager.getLogger()

    private val root = fileService.service("db")
    private val options = Options()
    private val dbOptions = DBOptions()
        .setCreateIfMissing(true)
        .setCreateMissingColumnFamilies(true)

    private val writeOptions = WriteOptions().setSync(true)
    private val readOptions = ReadOptions()
    private val cfOptions = ColumnFamilyOptions().optimizeUniversalStyleCompaction()

    private val cfDescriptors = mutableListOf(
        ColumnFamilyDescriptor(RocksDB.DEFAULT_COLUMN_FAMILY, cfOptions)
    )
    private val cfHandles = mutableListOf<ColumnFamilyHandle>()

    init {
        RocksDB.listColumnFamilies(options, root.absolutePath).forEach {
            cfDescriptors += ColumnFamilyDescriptor(it, cfOptions)
        }
    }

    private val db = RocksDB.open(dbOptions, root.absolutePath, cfDescriptors, cfHandles)

    fun columnFamily(name: ByteArray): ColumnFamilyHandle {
        val handle = cfHandles.find { it.name!!.contentEquals(name) }
        return if (handle == null) {
            val descriptor = ColumnFamilyDescriptor(name, cfOptions)
            db.createColumnFamily(descriptor)
        } else {
            handle
        }
    }

    fun drop(cf: ColumnFamilyHandle) {
        db.dropColumnFamily(cf)
    }

    fun put(cf: ColumnFamilyHandle, key: ByteArray, value: ByteArray) {
        db.put(cf, writeOptions, key, value)
    }

    fun get(cf: ColumnFamilyHandle, key: ByteArray): ByteArray? {
        return db.get(cf, readOptions, key)
    }

    fun delete(cf: ColumnFamilyHandle, key: ByteArray) {
        return db.delete(cf, writeOptions, key)
    }

    fun iterator(cf: ColumnFamilyHandle): RocksIterator {
        return db.newIterator(cf, readOptions)
    }
}