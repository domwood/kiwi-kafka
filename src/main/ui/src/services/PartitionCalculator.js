// Partitioning function matching the Kafka Java's client default partitioning using the Murmur2 hash algorithm.
// https://github.com/apache/kafka/blob/1.0/clients/src/main/java/org/apache/kafka/clients/producer/internals/DefaultPartitioner.java

import { Buffer } from "buffer";

Buffer.from("anything", "base64");
window.Buffer = window.Buffer || require("buffer").Buffer;

const murmur2 = require('murmur-hash-js').murmur2
// The Java client's seed https://github.com/apache/kafka/blob/1.0/clients/src/main/java/org/apache/kafka/common/utils/Utils.java#L355
const SEED = 0x9747b28c

/**
 * Always returns a consistent positiv number
 * Used by the Java client's partitioning function https://github.com/apache/kafka/blob/1.0/clients/src/main/java/org/apache/kafka/common/utils/Utils.java#L741
 * @param {Number} n get's bit-wise multiplicated with 0x7fffffff
 * @returns {Number} which is n bit-wise multiplocated with 0x7fffffff
 */
const _toPositive = n => {
    return n & 0x7fffffff
}

/**
 * Returns the partition number according to the Kafka Java client's default partitioning function, based on a key and the number of partitions
 * @param {String} key used by the partitioning function
 * @param {Number} partitionCount number of total partitions
 * @returns {Number} partition number to write to
 */
export const partition = (key, partitionCount) => {
    // Has to be a buffer, otherwise fails for öäü etc., see: https://github.com/oleksiyk/kafka/blob/master/lib/assignment/partitioners/default.js#L32
    const buf = Buffer.isBuffer(key) ? key : Buffer.from(key)
    return _toPositive(murmur2(buf, SEED)) % partitionCount
}

