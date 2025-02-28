package br.com.rodrigogurgel.snowflakeid.common

import java.util.concurrent.atomic.AtomicLong

/**
 * Snowflake ID Generator.
 *
 * This class generates unique IDs using the Snowflake algorithm.
 * It ensures uniqueness and high performance in a distributed system.
 *
 * The generated ID is a 64-bit long number composed of:
 * - 41 bits: Timestamp (milliseconds since custom epoch)
 * - 5 bits: Datacenter ID
 * - 5 bits: Worker ID
 * - 12 bits: Sequence number within the same millisecond
 *
 * It includes logic to prevent duplicate IDs, handle time rollback, and optimize concurrency.
 *
 * @property epoch Custom epoch timestamp (milliseconds since a custom start time).
 * @property datacenterId Datacenter ID (5 bits, differentiates between datacenters).
 * @property workerId Worker ID (5 bits, differentiates between nodes within a datacenter).
 *
 * @constructor Creates a Snowflake ID generator with default or custom parameters.
 *
 * @author ChatGPT
 * @version 1.2.0
 */
class SnowflakeIdGenerator {
    private val epoch: Long
    private val datacenterId: Long
    private val workerId: Long

    companion object {
        private const val SEQUENCE_BITS = 12
        private const val WORKER_ID_BITS = 5
        private const val DATACENTER_ID_BITS = 5
        private const val MAX_SEQUENCE = (1L shl SEQUENCE_BITS) - 1
        private const val DEFAULT_EPOCH = 1609459200000L // Friday, 1 January 2021 00:00:00
        private const val DEFAULT_DATACENTER_ID = 0L
        private const val DEFAULT_WORKER_ID = 0L
    }

    private val sequence = AtomicLong(0)
    private val lastTimestamp = AtomicLong(-1L)

    /**
     * Default constructor using predefined epoch and default values.
     *
     * **Warning:** Ensure only one instance of this class is used per application to prevent duplicate IDs.
     */
    constructor() : this(DEFAULT_EPOCH, DEFAULT_DATACENTER_ID, DEFAULT_WORKER_ID)

    /**
     * Constructor with custom epoch and datacenter ID.
     * Assigns a random worker ID.
     *
     * **Warning:** Ensure only one instance of this class is used per application to prevent duplicate IDs.
     *
     * @param epoch Custom epoch timestamp.
     * @param datacenterId Unique datacenter ID.
     */
    constructor(epoch: Long, datacenterId: Long) :
        this(epoch, datacenterId, (Math.random() * (1 shl WORKER_ID_BITS)).toLong())

    /**
     * Constructor with custom epoch, datacenter ID, and worker ID.
     *
     * **Warning:** Ensure only one instance of this class is used per application to prevent duplicate IDs.
     *
     * @param epoch Custom epoch timestamp.
     * @param datacenterId Unique datacenter ID.
     * @param workerId Unique worker ID.
     */
    constructor(epoch: Long, datacenterId: Long, workerId: Long) {
        this.epoch = epoch
        this.datacenterId = datacenterId
        this.workerId = workerId
    }

    /**
     * Generates the next unique ID.
     *
     * @return A unique 64-bit Snowflake ID.
     */
    fun nextId(): Long = generateId(this.workerId)

    /**
     * Generates the next unique ID with a specific worker ID.
     *
     * @param workerId The worker ID to use for this ID generation.
     * @return A unique 64-bit Snowflake ID.
     */
    fun nextId(workerId: Long): Long = generateId(workerId)

    private fun generateId(workerId: Long): Long {
        var timestamp = System.currentTimeMillis()
        var sequenceLocal: Long

        while (true) {
            val lastTimestampLocal = lastTimestamp.get()

            if (timestamp < lastTimestampLocal) {
                timestamp = waitUntilNextMillis(lastTimestampLocal)
            }

            if (timestamp == lastTimestampLocal) {
                sequenceLocal = sequence.incrementAndGet() and MAX_SEQUENCE
                if (sequenceLocal == 0L) {
                    timestamp = waitUntilNextMillis(lastTimestampLocal)
                }
            } else {
                sequence.set(0)
                sequenceLocal = 0
            }

            if (lastTimestamp.compareAndSet(lastTimestampLocal, timestamp)) {
                break
            }
        }

        return ((timestamp - epoch) shl (WORKER_ID_BITS + DATACENTER_ID_BITS + SEQUENCE_BITS)) or
            (datacenterId shl (WORKER_ID_BITS + SEQUENCE_BITS)) or
            (workerId shl SEQUENCE_BITS) or
            sequenceLocal
    }

    /**
     * Waits until the next millisecond if the sequence overflows.
     *
     * @param lastTimestamp The last used timestamp.
     * @return The next valid timestamp.
     * @throws ClockMovedBackwardsException if the system clock moved backwards.
     */
    private fun waitUntilNextMillis(lastTimestamp: Long): Long {
        var timestamp: Long
        do {
            timestamp = System.currentTimeMillis()
            if (timestamp < lastTimestamp) {
                throw ClockMovedBackwardsException(lastTimestamp, timestamp)
            }
            Thread.onSpinWait()
        } while (timestamp <= lastTimestamp)
        return timestamp
    }

    data class ClockMovedBackwardsException(
        private val lastTimestamp: Long,
        private val currentTimestamp: Long,
    ) : RuntimeException("Clock moved backwards. Last timestamp: [$lastTimestamp], current timestamp is: [$currentTimestamp]")
}
