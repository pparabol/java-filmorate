# java-filmorate
Template repository for Filmorate project.
## ER-диаграмма 
![ER-diagram](https://im.wampi.ru/2023/05/05/ER-giagram.png)
## Примеры запросов:
### *Получение пользователя по id*

    SELECT
        email,
        login,
        name,
        birthday
    FROM user
    WHERE user_id=1;
### *Получение всех фильмов*

    SELECT name
    FROM film;
### *Получение топ-10 популярных фильмов*
    
    SELECT name
    FROM film
    WHERE film_id IN
        (SELECT film_id
        FROM like
        ORDER BY COUNT(user_id) DESC
        LIMIT 10);
