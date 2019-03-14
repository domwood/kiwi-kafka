package com.github.domwood.kiwi.kafka.task.consumer;

import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.data.output.ImmutableConsumerResponse;
import com.github.domwood.kiwi.kafka.exceptions.ConsumerDataTooLargeException;
import com.github.domwood.kiwi.kafka.filters.FilterBuilder;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.function.Predicate;

import static com.github.domwood.kiwi.kafka.utils.KafkaUtils.fromKafkaHeaders;
import static com.github.domwood.kiwi.utilities.FutureUtils.failedFuture;
import static java.time.temporal.ChronoUnit.SECONDS;

public class BasicConsumeMessages implements KafkaTask<ConsumerRequest, List<ConsumerResponse<String, String>>, KafkaConsumerResource<String, String>> {

    //TODO Add configuration mechanism
    private final Integer maxDataSize = 1024 /*Kb*/ * 8 /*Bytes*/; /*Max Bits*/
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public CompletableFuture<List<ConsumerResponse<String, String>>> execute(KafkaConsumerResource<String, String> resource,
                                                                             ConsumerRequest input) {

        try{
            KafkaConsumer<String, String> consumer = resource.provisionResource();
            consumer.subscribe(input.topics());
            Set<TopicPartition> topicPartitionSet = consumer.assignment();
            consumer.seekToBeginning(topicPartitionSet);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitionSet);

            Queue<ConsumerResponse<String, String>> queue;
            if(input.limit() > 0 && !input.limitAppliesFromStart()){
                queue = new CircularFifoQueue<>(input.limit());
            }
            else{
                queue = new LinkedList<>();
            }

            boolean running = true;
            int pollEmptyCount = 0;
            int approximateSizeInBits = 0;
            Predicate<ConsumerRecord<String, String>> filter = input.filter().map(FilterBuilder::compileFilter)
                    .orElse(FilterBuilder.dummyFilter());


            while(running){
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(1, SECONDS));
                if(records.isEmpty()){
                    pollEmptyCount++;
                }
                else{
                    Iterator<ConsumerRecord<String, String>> recordIterator = records.iterator();
                    Map<TopicPartition, OffsetAndMetadata> toCommit = new HashMap<>();
                    while(recordIterator.hasNext() && (queue.size() < input.limit() || input.limit() < 1)){
                        ConsumerRecord<String, String> record = recordIterator.next();
                        String payload = record.value();
                        if(filter.test(record)){
                            approximateSizeInBits += payload != null ? payload.length() * 16 : 0;
                            queue.add(ImmutableConsumerResponse.<String, String>builder()
                                    .key(record.key())
                                    .message(payload)
                                    .headers(fromKafkaHeaders(record.headers()))
                                    .build());

                            toCommit.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
                            if(approximateSizeInBits > maxDataSize){
                                consumer.commitAsync(toCommit, this::logCommit);
                                throw new ConsumerDataTooLargeException();
                            }
                        }
                    }
                    consumer.commitAsync(toCommit, this::logCommit);

                    boolean maxQueueReached = queue.size() >= input.limit() && input.limit() > 1 && input.limitAppliesFromStart();
                    running = ! ( maxQueueReached || pollEmptyCount > 3 || isEndofData(endOffsets, toCommit));
                }
            }
        }
        catch (Exception e){
            logger.error("Failed to complete task of consuming from topics " + input.topics(), e);
            resource.discard();
            return failedFuture(e);
        }
        return null;
    }

    private void logCommit(Map<TopicPartition, OffsetAndMetadata> offsetData, Exception exception){
        if(exception != null){
            logger.error("Failed to commit offset ", exception);
        }
        else{
            logger.debug("Commit offset data {}", offsetData);
        }
    }

    private boolean isEndofData(Map<TopicPartition, Long> endOffsets, Map<TopicPartition, OffsetAndMetadata> lastCommit){
        return endOffsets.entrySet().stream()
                .allMatch(kv -> {
                    if(kv.getValue() < 1){
                        return true;
                    }
                    else{
                        OffsetAndMetadata latestCommit = lastCommit.get(kv.getKey());
                        return latestCommit != null && latestCommit.offset()-1 >= kv.getValue();
                    }
                });
    }

}
