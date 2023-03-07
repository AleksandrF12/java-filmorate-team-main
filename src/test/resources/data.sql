--удаляем все данные и обнуляем счётчики первичных ключей
DELETE FROM FILMS_GENRE;
DELETE FROM GENRE;
DELETE FROM FILMS_LIKE;
DELETE FROM FILMS;
DELETE FROM RATINGS_MPA;
DELETE FROM FRIENDS;
DELETE FROM USERS;

ALTER TABLE FILMS ALTER COLUMN film_id RESTART WITH 1;
ALTER TABLE RATINGS_MPA ALTER COLUMN rating_id RESTART WITH 1;
ALTER TABLE FILMS_GENRE ALTER COLUMN films_genre_id RESTART WITH 1;
ALTER TABLE FILMS_LIKE ALTER COLUMN films_like_id RESTART WITH 1;
ALTER TABLE FRIENDS ALTER COLUMN user_friend_id RESTART WITH 1;
ALTER TABLE GENRE ALTER COLUMN genre_id RESTART WITH 1;
ALTER TABLE USERS ALTER COLUMN user_id RESTART WITH 1;

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


--заполняем таблицу users
INSERT INTO users(email,login,name,birthday) VALUES('user1@mail.ru','user1_login','user1_name','1990-12-01'),
                                                    ('user2@yandex.ru','user2_login','user2_name','2000-05-09'),
                                                    ('user3@gmail.ru','user3_login','user3_name','1995-07-03'),
                                                    ('user4@mail.ru','user4_login','user4_name','1980-03-25'),
                                                    ('user5@yandex.ru','user5_login','user5_name','1985-11-15'),
                                                    ('user6@mail.ru','user6_login','user6_name','2002-08-11');

--заполняем таблицу films
INSERT INTO films(name,description,release_date,duration,rate,rating_id)
VALUES('Interstellar',concat('Earth future has been riddled by disasters, famines, and droughts. ',
        'There is only one way to ensure mankind survival: Interstellar travel.'),'2014-11-06',169,9,3),
      ('The Green Mile',concat('Based on Stephen King 1996 novel of the same name. Stars Tom Hanks as a death ',
                   'row corrections officer during the U.S. Great Depression who witnesses supernatural events that occur ',
                   'after an enigmatic inmate (Michael Clarke Duncan) is brought to his facility.'),'1999-12-6',189,10,4),
      ('Back to the Future',concat('Marty McFly, a typical American teenager of the Eighties, is accidentally sent back ',
      'to 1955 in a plutonium-powered DeLorean "time machine" invented by a slightly mad scientist. During his often',
       'hysterical, always amazing trip back in time, Marty must make certain his teenage parents-to-be meet and fall ',
       'in love - so he can get back to the future.'),'1985-07-03',116,8,2),
      ('The Lion King',concat('A young lion prince is cast out of his pride by his cruel uncle, who claims he ',
      'killed his father. While the uncle rules with an iron paw, the prince grows up beyond the Savannah, living by ',
      'a philosophy: No worries for the rest of your days.'),'1994-06-12',88,5,1);

--заполняем таблицу films_genre
INSERT INTO films_genre(film_id,genre_id) VALUES(1,2),
                                                (2,2),(2,4),
                                                (3,1),(3,4),(3,6),
                                                (4,2),(4,3);

--заполняем таблицу friends
INSERT INTO friends(user_id,friend_id,friend_status) VALUES(1,2,true),
                                                            (1,3,false),
                                                            (4,1,false),
                                                            (4,2,true),
                                                            (2,5,false),
                                                            (3,4,true);

--заполняем таблицу films_like
INSERT INTO films_like(film_id,user_id) VALUES(1,2),
                                            (1,5),
                                            (2,3),
                                            (2,4),
                                            (2,5),
                                            (3,6);
