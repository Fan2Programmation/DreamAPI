# Java API for HTTP Interface between Android App and PSQL Database

This project provides a Java API to facilitate communication between an Android app and a PostgreSQL database using HTTP requests. It's built with **Spring Boot** and offers endpoints for user registration and login.

## How to Run the Server

To start the server, open a terminal/command prompt and run the following command:

```bash
./mvnw spring-boot:run
```

This will start the Spring Boot application on your local machine, typically accessible at `http://localhost:8080`.

## Features

- **User Registration** (`/passerelle/v1/users/register`)
- **User Login** (`/passerelle/v1/users/login`)

Both endpoints interact with a PostgreSQL database to store and authenticate users.

## Requirements

- **Java** (JDK 11 or above)
- **Maven** (for building the project)
- **PostgreSQL** (or any other preferred database if configured)

## Setup

1. Clone this repository to your local machine.
2. Ensure you have a PostgreSQL database running.
3. Set up database connection in `application.properties`.
4. Run the command above to start the server.

## Created by

**Thibault Terrié**

Feel free to reach out if you need any further assistance with this project!
