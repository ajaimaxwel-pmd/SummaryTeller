package org.theMaxwellGlobe;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

class Review {
    String id;
    String message;
    String language;
    Date createdOn;

    @Override
    public String toString() {
        return "Review{" +
                "id='" + id + '\'' +
                ", message='" + message + '\'' +
                ", language='" + language + '\'' +
                ", createdOn=" + createdOn +
                '}';
    }
}

class Game {
    String id;
    String name;
    Date releasedOn;
    List<Review> reviews;
    double reviewScore;
    String reviewScoreDescription;
    int totalReviews;
    int totalPositiveReviews;
    int totalNegativeReviews;
    boolean reviewsSynced;

    String nextCursor; // to avoid making duplicate calls

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String releasedOnStr = (releasedOn != null) ? sdf.format(releasedOn) : "null";

        String reviewsStr = (reviews != null) ? reviews.stream()
                .map(Review::toString)
                .collect(Collectors.joining(", ", "[", "]"))
                : "null";

        return "Game{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", releasedOn=" + releasedOnStr +
                ", reviews=" + reviewsStr +
                ", reviewScore=" + reviewScore +
                ", reviewScoreDescription='" + reviewScoreDescription + '\'' +
                ", totalReviews=" + totalReviews +
                ", totalPositiveReviews=" + totalPositiveReviews +
                ", totalNegativeReviews=" + totalNegativeReviews +
                ", nextCursor='" + nextCursor + '\'' +
                '}';
    }
}

class JsonReview {
    String recommendationid;
    String review;
    boolean voted_up;
    int votes_up;
    int votes_funny;
    double weighted_vote_score;
    String language;
    int timestamp_created;
}

class JsonReviewSummary {
    int num_reviews;
    double review_score;
    String review_score_desc;
    int total_positive;
    int total_negative;
    int total_reviews;
}

class SteamJsonResponse {
    int success;
    JsonReviewSummary query_summary;
    List<JsonReview> reviews;
    String cursor;
}

public class SteamHandler {
    public void getGameReviews(String gameId, KafkaProducer<String, String> producer) {
        // steam limits each api call with 20 reviews
        boolean hasMoreReviews = true;
        String nextCursor = "0";
        List<String> reviews = new ArrayList<String>();
        double reviewScore = 0.0;
        String reviewScoreDescription;
        int totalReviews = 0;
        int totalPositiveReviews = 0;
        int totalNegativeReviews = 0;
//        game.reviews = new ArrayList<Review>();

        while (hasMoreReviews) {
            try {
                String url = (nextCursor.equals("0")) ?
                        String.format("http://store.steampowered.com/appreviews/%s?json=1&start_offset=%s", gameId, nextCursor)
                        : String.format("http://store.steampowered.com/appreviews/%s?json=1&cursor=%s", gameId, nextCursor);

                HttpClient client = HttpClient.newBuilder()
                        .followRedirects(HttpClient.Redirect.NORMAL)
                        .build();

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(url))
                        .build();

                System.out.println("Calling " + url);
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                System.out.println("Status Code: " + response.statusCode());
//                System.out.println("Response Body: \n" + response.body());

                Game game = parseJsonToGame(response.body(), nextCursor.equals("0"));
                // PRODUCE GAME
                String topic = "steam-reviews-2";
                Gson gson = new Gson();
                String jsonValue = gson.toJson(game);
                System.out.println("jsonValue------" + jsonValue);
                producer.send(new ProducerRecord<>(topic, gameId, jsonValue));

                String cursor = getResponseValueByKey(response.body(), "cursor");
                if (cursor != null) {
                    nextCursor = formatCursor(cursor);
//                    hasMoreReviews = false; // Remove later
                } else {
                    hasMoreReviews = false;
                }
            } catch (IOException | InterruptedException e) {
                hasMoreReviews = false;
                e.printStackTrace();
            }
        }
    }

    private static String getResponseValueByKey(String responseBody, String key) {
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        if (jsonResponse.has(key) && !jsonResponse.get("cursor").isJsonNull()) {
            return jsonResponse.get(key).getAsString();
        } else {
            return null;
        }
    }

    private static JsonObject getSummary(String responseBody) {
        JsonObject jsonResponse = JsonParser.parseString(responseBody).getAsJsonObject();

        if (jsonResponse.has("query_summary") && !jsonResponse.get("query_summary").isJsonNull()) {
            return jsonResponse.getAsJsonObject("query_summary");
        } else {
            return null;
        }
    }

    public static Game parseJsonToGame(String json, boolean setSummary) {
        Gson gson = new Gson();
        SteamJsonResponse jsonResponse = gson.fromJson(json, SteamJsonResponse.class);
        Game game = new Game();
        game.reviews = new ArrayList<>();
        if (setSummary) { // Only the first call to steam returns summary
            game.reviewScoreDescription = jsonResponse.query_summary.review_score_desc;
            game.reviewScore = jsonResponse.query_summary.review_score;
            game.totalReviews =jsonResponse.query_summary.total_reviews;
            game.totalPositiveReviews = jsonResponse.query_summary.total_positive;
            game.totalNegativeReviews = jsonResponse.query_summary.total_negative;
        }

        for (JsonReview info : jsonResponse.reviews) {
            if (Objects.equals(info.language, "english")) {
                Review review = new Review();
                review.id = info.recommendationid;
                review.message = info.review;
                review.language = info.language;
                review.createdOn = new Date( info.timestamp_created *  1000L);

                game.reviews.add(review);
            }
        }
        return game;
    }

    private String formatCursor(String c) {
        // AoIIPwKChHSp+NQE -> AoIIPwKChHSpNQE; AoIIPxOxOnT1pcIE -> AoIIPxOxOnT1pcIE
        String[] parts = c.split("\\+");
        return String.join("", parts);
    }
}
