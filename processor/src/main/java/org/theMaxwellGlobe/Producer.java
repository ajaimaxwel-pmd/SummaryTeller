package org.theMaxwellGlobe;

import org.apache.kafka.clients.producer.KafkaProducer;

import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class Producer {
    public static void main(String[] args) throws SQLException {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        KafkaProducer<String, String> producer = new KafkaProducer<>(props);

        SteamHandler steamHandler = new SteamHandler();
        GameDao gameDao = new GameDao();
        List<Game> games = gameDao.find();
        for (Game game: games) {
            steamHandler.getGameReviews(game.id, producer);
        }
//        steamHandler.getGameReviews("20", producer);
        producer.close();
    }
}
