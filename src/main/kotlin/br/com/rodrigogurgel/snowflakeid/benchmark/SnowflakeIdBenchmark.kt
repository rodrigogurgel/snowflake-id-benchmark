package br.com.rodrigogurgel.snowflakeid.benchmark

import br.com.rodrigogurgel.snowflakeid.common.SnowflakeIdGenerator
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
import java.util.concurrent.TimeUnit

@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 1, time = 2, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 10, timeUnit = TimeUnit.SECONDS)
@Suppress("unused")
@OutputTimeUnit(TimeUnit.MILLISECONDS)
class SnowflakeIdBenchmark {
    private val snowflakeIdGenerator = SnowflakeIdGenerator(System.currentTimeMillis(), 0)

    @Benchmark
    @BenchmarkMode(Mode.Throughput)
    fun generateSingleSnowflakeId() {
        snowflakeIdGenerator.nextId(Thread.currentThread().id)
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun generateBatchSnowflakeIds() {
        List(1_000) { snowflakeIdGenerator.nextId(Thread.currentThread().id) }
    }

    @Benchmark
    @BenchmarkMode(Mode.AverageTime)
    fun generateBatchSnowflakeIdsParallel() =
        runBlocking {
            val batchSize = 1_000
            val results = mutableListOf<Long>()
            val mutex = Mutex()

            withContext(Dispatchers.Default) {
                val jobs =
                    List(batchSize) {
                        async {
                            val id = snowflakeIdGenerator.nextId(Thread.currentThread().id)
                            mutex.withLock { results.add(id) }
                        }
                    }
                jobs.awaitAll()
            }
            results
        }
}
