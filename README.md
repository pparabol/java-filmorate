# java-filmorate
Template repository for Filmorate project.
## ER-диаграмма 
![ER-diagram](https://ic.wampi.ru/2023/05/18/ER-diagram.png)
## Примеры запросов:
### *Получение пользователя по id*

    SELECT
        email,
        login,
        name,
        birthday
    FROM users
    WHERE user_id=1;
### *Получение всех фильмов*

    SELECT *
    FROM films;
### *Получение топ-10 популярных фильмов*
    
    SELECT name
    FROM films
    WHERE film_id IN
        (SELECT film_id
        FROM likes
        GROUP BY film_id
        ORDER BY COUNT(user_id) DESC
        LIMIT 10);
