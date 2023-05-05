# java-filmorate
Template repository for Filmorate project.
## ER-диаграмма 
![ER-diagram](file:///C:/Users/Полина/Downloads/ER%20giagram.PNG)
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
        ORDER BY SUM(user_id) DESC
        LIMIT 10);
