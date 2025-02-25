package com.appdynamics.extensions.kafka;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;

public class ConsumerGroupLag {

static String host = "localhost:9092";
static String topic = "topic02";
static String groupId = "test-group";

public static void main(String... vj) {
    ConsumerGroupLag cgl = new ConsumerGroupLag();

    while (true) {
        Map<TopicPartition, PartionOffsets> lag = cgl.getConsumerGroupOffsets(host, topic, groupId);
        System.out.println("$$LAG = " + lag);
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

private final String monitoringConsumerGroupID = "monitoring_consumer_" + UUID.randomUUID().toString();

public Map<TopicPartition, PartionOffsets> getConsumerGroupOffsets(String host, String topic, String groupId) {
    Map<TopicPartition, Long> logEndOffset = getLogEndOffset(topic, host);

    Set<TopicPartition> topicPartitions = new HashSet<>();
    for (Entry<TopicPartition, Long> s : logEndOffset.entrySet()) {
        topicPartitions.add(s.getKey());
    }
    
    KafkaConsumer<String, Object> consumer = createNewConsumer(groupId, host);
    Map<TopicPartition, OffsetAndMetadata> comittedOffsetMeta = consumer.committed(topicPartitions);

    BinaryOperator<PartionOffsets> mergeFunction = (a, b) -> {
        throw new IllegalStateException();
    };
    Map<TopicPartition, PartionOffsets> result = logEndOffset.entrySet().stream()
            .collect(Collectors.toMap(entry -> (entry.getKey()), entry -> {
                OffsetAndMetadata committed = comittedOffsetMeta.get(entry.getKey());
                long currentOffset = 0;
                if(committed != null) { //committed offset will be null for unknown consumer groups
                    currentOffset = committed.offset();
                }
                return new PartionOffsets(entry.getValue(), currentOffset, entry.getKey().partition(), topic);
            }, mergeFunction));

    return result;
}

public Map<TopicPartition, Long> getLogEndOffset(String topic, String host) {
    Map<TopicPartition, Long> endOffsets = new ConcurrentHashMap<>();
    KafkaConsumer<?, ?> consumer = createNewConsumer(monitoringConsumerGroupID, host);
    List<PartitionInfo> partitionInfoList = consumer.partitionsFor(topic);
    List<TopicPartition> topicPartitions = partitionInfoList.stream()
            .map(pi -> new TopicPartition(topic, pi.partition())).collect(Collectors.toList());
    consumer.assign(topicPartitions);
    consumer.seekToEnd(topicPartitions);
    topicPartitions.forEach(topicPartition -> endOffsets.put(topicPartition, consumer.position(topicPartition)));
    consumer.close();
    return endOffsets;
}

private static KafkaConsumer<String, Object> createNewConsumer(String groupId, String host) {
    Properties properties = new Properties();
    properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, host);
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
    properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
    return new KafkaConsumer<>(properties);
}

private static class PartionOffsets {
    private long lag;
    private long timestamp = System.currentTimeMillis();
    private long endOffset;
    private long currentOffset;
    private int partion;
    private String topic;

    public PartionOffsets(long endOffset, long currentOffset, int partion, String topic) {
        this.endOffset = endOffset;
        this.currentOffset = currentOffset;
        this.partion = partion;
        this.topic = topic;
        this.lag = endOffset - currentOffset;
    }

    @Override
    public String toString() {
        return "PartionOffsets [lag=" + lag + ", timestamp=" + timestamp + ", endOffset=" + endOffset
                + ", currentOffset=" + currentOffset + ", partion=" + partion + ", topic=" + topic + "]";
    }

}
}