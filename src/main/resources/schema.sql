CREATE TABLE IF NOT EXISTS users (
user_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
email varchar NOT NULL,
login varchar NOT NULL,
name varchar,
birthday date NOT NULL
);

CREATE TABLE IF NOT EXISTS mpa (
mpa_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS films (
film_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name varchar NOT NULL,
description varchar(200) NOT NULL,
release_date date NOT NULL,
duration integer NOT NULL,
mpa_id INTEGER REFERENCES mpa(mpa_id),
CONSTRAINT check_duration CHECK (duration >= 0)
);

CREATE TABLE IF NOT EXISTS likes (
film_id INTEGER NOT NULL REFERENCES films(film_id),
user_id INTEGER NOT NULL REFERENCES users(user_id),
PRIMARY KEY(film_id, user_id)
);

CREATE TABLE IF NOT EXISTS genres (
genre_id INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
name varchar NOT NULL
);

CREATE TABLE IF NOT EXISTS film_genres (
film_id INTEGER NOT NULL REFERENCES films(film_id),
genre_id INTEGER NOT NULL REFERENCES genres(genre_id),
PRIMARY KEY(film_id, genre_id)
);

CREATE TABLE IF NOT EXISTS friends (
user_id INTEGER NOT NULL REFERENCES users(user_id),
friend_id INTEGER NOT NULL REFERENCES users(user_id),
is_accepted boolean NOT NULL,
PRIMARY KEY(user_id, friend_id)
);