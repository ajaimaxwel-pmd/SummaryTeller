import psycopg2


class Game:
    def __init__(self, db_connection):
        self.db_connection = db_connection

    def get_games(self):
        conn = self.db_connection.conn
        if conn is None:
            print("Database connection is not established.")
            return []
        try:
            with conn.cursor() as cursor:
                cursor.execute("SELECT id, steam_id, name, released_on, review_score, review_score_description, "
                               "total_reviews,"
                               "total_positive_reviews, total_positive_reviews FROM games")
                games = cursor.fetchall()
                return games
        except psycopg2.Error as e:
            print(f"Failed to fetch games: {e}")
            return []

    def get_reviews_for_game(self, game_id):
        conn = self.db_connection.conn
        if conn is None:
            print("Database connection is not established.")
            return []
        try:
            with conn.cursor() as cursor:
                cursor.execute("SELECT message FROM reviews WHERE game_id::integer = %s ORDER BY created_on DESC "
                               "LIMIT 100", (game_id,))
                reviews = cursor.fetchall()
                return reviews
        except psycopg2.Error as e:
            print(f"Failed to fetch reviews for game {game_id}: {e}")
            return []

    def update_game_summary(self, game_id, summary):
        try:
            with self.db_connection.conn.cursor() as cursor:
                update_query = """
                UPDATE games
                SET review_summary = %s
                WHERE id = %s;
                """
                cursor.execute(update_query, (summary, game_id))
                self.db_connection.conn.commit()  # Commit the transaction
                print(f"Summary for game {game_id} updated successfully.")
        except Exception as e:
            print(f"Failed to update summary for game {game_id}: {e}")
