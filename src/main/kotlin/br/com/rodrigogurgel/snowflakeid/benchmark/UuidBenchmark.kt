package br.com.rodrigogurgel.snowflakeid.benchmark

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.openjdk.jmh.annotations.Benchmark
import org.openjdk.jmh.annotations.BenchmarkMode
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Mode
import org.openjdk.jmh.annotations.OutputTimeUnit
import org.openjdk.jmh.annotations.Scope
import org.openjdk.jmh.annotations.State
import org.openjdk.jmh.annotations.Warmup
import java.util.UUID
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Suppress("unused")
class UuidBenchmark {
    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun generateSingleUUID() {
        UUID.randomUUID()
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun generateBatchUUIDs() {
        List(1_000) { UUID.randomUUID() }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun generateBatchUUIDsParallel() =
        runBlocking {
            val batchSize = 1_000
            val results = mutableListOf<UUID>()
            val mutex = Mutex()

            withContext(Dispatchers.Default) {
                val jobs =
                    List(batchSize) {
                        async {
                            val id = UUID.randomUUID()
                            mutex.withLock { results.add(id) }
                        }
                    }
                jobs.awaitAll()
            }
            results
        }
}
