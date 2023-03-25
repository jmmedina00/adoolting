# Adoolting

![](./src/main/resources/logo.svg)

Spring Boot, Thymeleaf, Docker, MariaDB, Redis, JUnit

[![](./src/main/resources/readme/spring.png)](https://spring.io/)
[![](./src/main/resources/readme/thymeleaf.png)](https://www.thymeleaf.org/)
[![](./src/main/resources/readme/docker-icon.png)](https://www.docker.com/)
[![](./src/main/resources/readme/mariadb-icon.png)](https://mariadb.org/)
[![](./src/main/resources/readme/redis.png)](https://redis.io/)
[![](./src/main/resources/readme/junit5.png)](https://junit.org/junit5/)

Social network experiment fully done with static pages as an artificial constraint.

It is "functional", yet incomplete. Navigation and certain features are not fully implemented. I decided to move on due to change in circumstances and this project dragging on for too long.

Partial prototype of UI is available at [adoolting-screens](https://github.com/jmmedina00/adoolting-screens).

## Prerequisites

VSCode: Make sure to enable AWT development: https://code.visualstudio.com/docs/java/java-gui

Install Docker Compose: https://docs.docker.com/compose/install/

## Testing

VSCode testing works just fine. Not tested with IntelliJ IDEA.

```
mvn test
```

## Running

Fire up Docker stack:

```
docker compose up
```

Run application (if not running from IDE):

```
mvn spring-boot:run
```

Run formatter:

```
mvn prettier:write
```
