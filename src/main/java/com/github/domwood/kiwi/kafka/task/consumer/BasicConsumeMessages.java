package com.github.domwood.kiwi.kafka.task.consumer;

import com.github.domwood.kiwi.data.input.ConsumerRequest;
import com.github.domwood.kiwi.data.output.ConsumedMessages;
import com.github.domwood.kiwi.data.output.ConsumerResponse;
import com.github.domwood.kiwi.data.output.ImmutableConsumedMessages;
import com.github.domwood.kiwi.data.output.ImmutableConsumerResponse;
import com.github.domwood.kiwi.kafka.exceptions.ConsumerDataTooLargeException;
import com.github.domwood.kiwi.kafka.filters.FilterBuilder;
import com.github.domwood.kiwi.kafka.resources.KafkaConsumerResource;
import com.github.domwood.kiwi.kafka.task.KafkaTask;
import com.github.domwood.kiwi.utilities.FutureUtils;
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
import static java.time.temporal.ChronoUnit.MILLIS;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

public class BasicConsumeMessages implements KafkaTask<ConsumerRequest, ConsumerResponse<String, String>, KafkaConsumerResource<String, String>> {

    //TODO Add configuration mechanism
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public CompletableFuture<ConsumerResponse<String, String>> execute(KafkaConsumerResource<String, String> resource,
                                                                       ConsumerRequest input) {
        return FutureUtils.supplyAsync(() -> executeSync(resource, input));
    }

    private ConsumerResponse<String, String> executeSync(KafkaConsumerResource<String, String> resource,
                                                         ConsumerRequest input) {

        try{
            KafkaConsumer<String, String> consumer = resource.provisionResource();
            consumer.subscribe(input.topics());
            Set<TopicPartition> topicPartitionSet = consumer.assignment();
            while(topicPartitionSet.isEmpty()){
                consumer.poll(Duration.of(1, MILLIS));
                topicPartitionSet = consumer.assignment();
            }
            consumer.seekToBeginning(topicPartitionSet);
            Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitionSet);

            Queue<ConsumedMessages<String, String>> queue;
            if(input.limit() > 0 && !input.limitAppliesFromStart()){
                queue = new CircularFifoQueue<>(input.limit());
            }
            else{
                queue = new LinkedList<>();
            }

            boolean running = true;
            int pollEmptyCount = 0;
            Predicate<ConsumerRecord<String, String>> filter = input.filter().map(FilterBuilder::compileFilter)
                    .orElse(FilterBuilder.dummyFilter());


            while(running){
                ConsumerRecords<String, String> records = consumer.poll(Duration.of(200, MILLIS));
                if(records.isEmpty() ){
                    pollEmptyCount++;
                }
                else{
                    pollEmptyCount = 0;
                    Iterator<ConsumerRecord<String, String>> recordIterator = records.iterator();
                    Map<TopicPartition, OffsetAndMetadata> toCommit = new HashMap<>();
                    while(recordIterator.hasNext() && (queue.size() < input.limit() || input.limit() < 1 || !input.limitAppliesFromStart())){
                        ConsumerRecord<String, String> record = recordIterator.next();
                        String payload = record.value();
                        if(filter.test(record)){
                            queue.add(ImmutableConsumedMessages.<String, String>builder()
                                    .timestamp(record.timestamp())
                                    .offset(record.offset())
                                    .partition(record.partition())
                                    .key(record.key())
                                    .message(payload)
                                    .headers(fromKafkaHeaders(record.headers()))
                                    .build());

                            toCommit.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()));
                        }
                    }
                    if(!toCommit.isEmpty()){
                        consumer.commitAsync(toCommit, this::logCommit);
                    }

                    boolean maxQueueReached = input.limit() > 1 && input.limitAppliesFromStart();
                    boolean endOfData = isEndofData(endOffsets, toCommit);
                    running = ! ( maxQueueReached || endOfData );

                    if(maxQueueReached) logger.debug("Max queue size reached");
                    if(maxQueueReached) logger.debug("End of data reached");
                }
                if(pollEmptyCount >= 3){
                    running = false;
                }
            }

            resource.discard();
            return ImmutableConsumerResponse.<String, String>builder()
                    .messages(queue.stream()
                            .sorted(Comparator.comparingLong(ConsumedMessages::timestamp))
                            .collect(toList()))
                    .build();
        }
        catch (Exception e){
            logger.error("Failed to complete task of consuming from topics " + input.topics(), e);
            resource.discard();
            return ImmutableConsumerResponse.<String, String>builder()
                    .messages(emptyList())
                    .build();
        }
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
