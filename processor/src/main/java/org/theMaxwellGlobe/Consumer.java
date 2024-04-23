package org.theMaxwellGlobe;

import com.google.gson.Gson;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;

public class Consumer {
    static {
        DatabaseConnectionPool.getDataSource();
    }

    private static final Logger logger = LoggerFactory.getLogger(Consumer.class);

    public static void main(String[] args) {
        logger.info("Starting consumer...");

        String topicName = "steam-reviews-2";
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", "test-group");
        props.put("enable.auto.commit", "true");
        props.put("auto.commit.interval.ms", "1000");
        props.put("auto.offset.reset", "earliest"); // Forces consumer to only read messages that are produced after it starts
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");

        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);

        consumer.subscribe(Arrays.asList(topicName));

        while (true) {
            ConsumerRecords<String, String> records = consumer.poll(100);
            for (ConsumerRecord<String, String> record : records) {
                Game game = parseGameMessage(record.value());
                for (Review review: game.reviews ) {
                    GameDao gameDao = new GameDao();
                    logger.info("Added new review to database" + review.message);
                    gameDao.addReview("20", review);
                }
            }
        }
    }

    private static void parseMessage(String json, Review review) {
        Gson gson = new Gson();
        Review jsonResponse = gson.fromJson(json, (Type) Review.class);

        review.id = jsonResponse.id;
        review.message = jsonResponse.message;
        review.language = jsonResponse.language;
        review.createdOn = new Date(String.valueOf(jsonResponse.createdOn));
    }

    private static Game parseGameMessage(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, Game.class);
    }
}
// https://github.com/cncgames/summarize-steam-reviews/blob/main/README.md