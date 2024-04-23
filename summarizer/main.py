from database.connect import DatabaseConnection
from Game import Game
from summarize_reviews import summarize_reviews


def fetch_reviews(game_db, game_id):
    try:
        reviews = game_db.get_reviews_for_game(game_id)
        messages = [row[0] for row in reviews]
        return messages
    except Exception as e:
        print(f"Failed to fetch reviews for game {game_id}: {e}")
        return []


def process_games(game_db):
    try:
        games = game_db.get_games()
    except Exception as e:
        print(f"Failed to fetch games: {e}")
        return

    for game in games:
        id_, steam_id, name, *_ = game

        # TODO: if game has already has summary, and requires_update flag is false don't update

        messages = fetch_reviews(game_db, steam_id)
        print(f"Reviews for game {name}: ", messages)

        if messages:
            summary = summarize_game_reviews(messages, 'bart')
            print(f"Summary for game {name}: ", summary)
            update_game_summary(game_db, id_, summary)


def summarize_game_reviews(messages, strategy):
    try:
        summary = summarize_reviews(messages, strategy)
        return summary
    except Exception as e:
        print(f"Error summarizing reviews: {e}")
        return "Summarization failed."


def update_game_summary(game_db, game_id, summary):
    try:
        game_db.update_game_summary(game_id, summary)
    except Exception as e:
        print(f"Error updating summary: {e}")
        return "Updating failed."


def main():
    db_conn = DatabaseConnection()
    try:
        game_db = Game(db_conn)
        process_games(game_db)
    finally:
        db_conn.close()


if __name__ == '__main__':
    main()
