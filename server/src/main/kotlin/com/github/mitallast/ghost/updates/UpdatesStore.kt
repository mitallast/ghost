package com.github.mitallast.ghost.updates

import com.github.mitallast.ghost.common.codec.Codec
import com.github.mitallast.ghost.common.codec.CodecMessage
import com.github.mitallast.ghost.persistent.PersistentService
import com.google.inject.Inject
import org.apache.logging.log4j.LogManager
import org.bouncycastle.util.encoders.Hex
import org.rocksdb.ColumnFamilyHandle

class UpdatesStore @Inject constructor(private val db: PersistentService) {
    private val logger = LogManager.getLogger()
    private val sequenceCF = db.columnFamily("updates.sequence".toByteArray())
    private val lastCF = db.columnFamily("updates.last".toByteArray())
    private val logMapCF = HashMap<String, ColumnFamilyHandle>()

    private fun logCF(auth: ByteArray): ColumnFamilyHandle {
        val key = Hex.toHexString(auth)
        return logMapCF.computeIfAbsent(key, {
            val name = "updates.log.$key"
            db.columnFamily(name.toByteArray())
        })
    }

    fun append(from: ByteArray, to: ByteArray, message: CodecMessage): Update {
        val current = currentSequence(to)
        val next = current + 1
        val update = Update(next, from, message)
        val data = Update.codec.write(update)
        val nextBuffer = Codec.longCodec().write(next)
        db.put(sequenceCF, to, nextBuffer)
        db.put(logCF(to), nextBuffer, data)
        return update
    }

    fun lastInstalled(auth: ByteArray): Long {
        val buffer = db.get(lastCF, auth)
        return if (buffer == null) {
            0L
        } else {
            Codec.longCodec().read(buffer)
        }
    }

    fun currentSequence(auth: ByteArray): Long {
        val sequenceBuffer = db.get(sequenceCF, auth)
        return if (sequenceBuffer != null) {
            Codec.longCodec().read(sequenceBuffer)
        } else {
            0L
        }
    }

    fun mark(auth: ByteArray, last: Long) {
        db.put(lastCF, auth, Codec.longCodec().write(last))
    }

    fun loadFrom(auth: ByteArray, last: Long, count: Int): List<Update> {
        val updates = ArrayList<Update>(count)
        val buffer = Codec.longCodec().write(last)
        val iterator = db.iterator(logCF(auth))
        iterator.seek(buffer)
        while (iterator.isValid && updates.size < count) {
            val currentBuffer = iterator.key()
            val current = Codec.longCodec().read(currentBuffer)
            logger.info("i=$current")
            if (current > last) {
                val update = Update.codec.read(iterator.value())
                updates.add(update)
            }
            iterator.next()
        }
        iterator.close()
        return updates
    }
}