--удаляем все данные и обнуляем счётчики первичных ключей
DELETE FROM FILMS_GENRE;
DELETE FROM GENRE;
DELETE FROM FILMS_LIKE;
DELETE FROM FILMS;
DELETE FROM RATINGS_MPA;
DELETE FROM FRIENDS;
DELETE FROM USERS;
DELETE FROM DIRECTORS;
DELETE FROM FILMS_DIRECTOR;

ALTER TABLE FILMS ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE RATINGS_MPA ALTER COLUMN rating_id RESTART WITH 1;
ALTER TABLE FILMS_GENRE ALTER COLUMN films_genre_id RESTART WITH 1;
ALTER TABLE FILMS_LIKE ALTER COLUMN films_like_id RESTART WITH 1;
ALTER TABLE FRIENDS ALTER COLUMN user_friend_id RESTART WITH 1;
ALTER TABLE GENRE ALTER COLUMN genre_id RESTART WITH 1;
ALTER TABLE USERS ALTER COLUMN user_id RESTART WITH 1;
ALTER TABLE DIRECTORS ALTER COLUMN director_id RESTART WITH 1;
ALTER TABLE FILMS_DIRECTOR ALTER COLUMN films_director_id RESTART WITH 1;

--заполняем таблицу ratings
INSERT INTO ratings_mpa(rating_name) VALUES('G'),
                                        ('PG'),
                                        ('PG-13'),
                                        ('R'),
                                        ('NC-17');

--заполянем таблицу genre
INSERT INTO genre(genre_name) VALUES('Комедия'),
                                    ('Драма'),
                                    ('Мультфильм'),
                                    ('Триллер'),
                                    ('Документальный'),
                                    ('Боевик');