package com.github.mitallast.ghost

import com.typesafe.config.ConfigFactory
import java.io.File
import java.util.concurrent.CountDownLatch

object Main {
    @JvmStatic
    fun main(args: Array<String>) {
        Registry.register()
        val file = File("application.conf").absoluteFile
        require(file.exists(), { file.path })
        val config = ConfigFactory.parseFile(file).resolve()
        val node = Server(config)
        node.start()

        val countDownLatch = CountDownLatch(1)
        Runtime.getRuntime().addShutdownHook(Thread {
            node.close()
            countDownLatch.countDown()
        })
        countDownLatch.await()
    }
}
