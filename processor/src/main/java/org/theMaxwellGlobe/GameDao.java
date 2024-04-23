package org.theMaxwellGlobe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GameDao {

    /**
     * Finds all games where reviews are synced.
     * @return ResultSet containing the found games.
     * @throws SQLException if there is an error fetching the data.
     */
    public List<Game> find() throws SQLException {
        String sql = "SELECT * FROM games WHERE reviews_synced = FALSE";
        List<Game> games = new ArrayList<>();

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Game game = new Game();
                game.id = rs.getString("steam_id");
                game.name = rs.getString("name");
                game.reviewsSynced = rs.getBoolean("reviews_synced");

                games.add(game);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error fetching games from database", e);
        }
        return games;
    }

    public void create(Game game) {
        String sql = "INSERT INTO games (id, name, released_on, review_score, review_score_description, total_reviews, total_positive_reviews, total_negative_reviews, next_cursor) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";

        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setObject(1, UUID.fromString(game.id));
            stmt.setString(2, game.name);
            stmt.setDate(3, java.sql.Date.valueOf(String.valueOf(game.releasedOn)));
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addReview(String gameId, Review review) {
        String sql = "INSERT INTO reviews (id, game_id, message, language, created_on) VALUES (?, ?, ?, ?, ?);";
        UUID uuid = UUID.randomUUID();
        try (Connection conn = DatabaseConnectionPool.getDataSource().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setObject(1, uuid);
            stmt.setObject(2, gameId);
            stmt.setString(3, review.message);
            stmt.setString(4, review.language);
            stmt.setDate(5, new java.sql.Date(review.createdOn.getTime()));
            stmt.executeUpdate();
        } catch (SQLException error) {
            System.out.println("Error adding review to database " +  error);
            error.printStackTrace();
        }
    }
    // Other CRUD methods...
}
