# Java API for HTTP Interface between Android App and PSQL Database

This project provides a Java API to facilitate communication between [Dreamcatcher](https://github.com/Fan2Programmation/Dreamcatcher) Android App and a PostgreSQL database using HTTP requests. It's built with **Spring Boot** and offers endpoints for Dreamcatching.

## How to Run the Server

Clone the repository to your machine and go into your new folder.

```bash
git clone https://github.com/Fan2Programmation/intermediate.git
cd intermediate
```

To start the server, open a terminal/command prompt and run the following commands:

```bash
./mvnw clean install
```

```bash
./mvnw spring-boot:run
```

This will start the Spring Boot application on your local machine, typically accessible at `http://localhost:8095`.

## Features

- **User Registration** (`/users/register`) *POST*
```json
{
  "username": "johndoe",
  "password": "mypassword"
}
```

- **User Login** (`/users/login`) *POST*
```json
{
  "username": "johndoe",
  "password": "mypassword"
}
```

- **Dream Entry** (`/dreams/create`) *POST*
```json
{
  "content": "I was flying over the rainbow!",
  "username": "johndoe"
}
```

- **Recent Dreams** (`/dreams/recent`) *GET*

- **Dream Search** (`/dreams/search?query={keyword}`) *GET*

- **Dream Fetch** (`/dreams/{id}`) *GET*

- **Dream Deletion** (`/dreams/delete/{id}?username={username}`) *DELETE*

Endpoints interact with a PostgreSQL database hosted on AlwaysData.

## Requirements

- **Java** (JDK 11 or above)
- **Maven** (for building the project)
- **PostgreSQL** (or any other preferred database if configured)

## Created by
**Thibault Terrié**

Feel free to reach out if you need any further assistance with this project!
