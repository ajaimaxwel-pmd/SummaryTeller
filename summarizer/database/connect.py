import psycopg2
from database.config import DATABASE_CONFIG


class DatabaseConnection:
    def __init__(self):
        self.conn = None
        self.cursor = None
        self.connect()

    def connect(self):
        try:
            self.conn = psycopg2.connect(**DATABASE_CONFIG)
            self.cursor = self.conn.cursor()
            print("Database connection established")
        except psycopg2.Error as e:
            print(f"Error: Could not make connection to the database")
            print(e)

    def get_cursor(self):
        return self.cursor

    def close(self):
        if self.conn:
            self.cursor.close()
            self.conn.close()
            print("Database connection closed")
