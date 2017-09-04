# Kotlin Dropwizard Pet License Server

> A simple REST service built with Kotlin and Dropwizard.


# Overview

This project features:

- Kotlin
- Dropwizard
- PostgreSQL
- Hibernate ORM
- Integration tests


# Requirements

- PostgreSQL (used 9.6.5)
- Java Development Kit 1.8.0

This service requires a database on localhost with the following configuration:

- **User credentials**: developer/developer
- **Database**: petLicense-dev
- **Schema**: public


# Getting Started

## Setup PostgreSQL

As a PostgreSQL admin, execute:

```sql
CREATE USER developer WITH
    PASSWORD 'developer'
    LOGIN
    NOSUPERUSER
    NOCREATEDB
    NOCREATEROLE
    INHERIT
    NOREPLICATION
    CONNECTION LIMIT -1;

CREATE DATABASE petLicense-dev
    WITH
    OWNER = developer
    ENCODING = 'UTF8'
    CONNECTION LIMIT = -1;
```

## Run Dropwizard Database Migrations

On first load, the Dropwizard database migrations need to be run **before running or testing the server**.

There are two ways you can do this:

1. Use your favorite IDE to make a run configuration for the following:

    - **Main class**: com.acme.petlicense.PetLicenseService
    - **Program arguments**: db migrate development.yml
    - **Use classpath of module**: kotlin-dropwizard-pet-license-server

2. Run `mvn package` to build a shaded jar, then run these arguments `java -jar <shaded.jar> db migrate development.yml`.

## Run Tests

Two options:

1. Use your favorite IDE to make a run configuration using junit:
2. Run `mvn test`.

## Run Server (Development)

Two options:

1. Use your favorite IDE to make a run configuration for the following:

    - **Main class**: com.acme.petlicense.PetLicenseService
    - **Program arguments**: server development.yml
    - **Use classpath of module**: kotlin-dropwizard-pet-license-server

2. Run `mvn package` to build a shaded jar, then run these arguments `java -jar <shaded.jar> server development.yml`.

While running, the web service will listen on http://localhost:8080 for all application requests and http://localhost:8081 for administration.
