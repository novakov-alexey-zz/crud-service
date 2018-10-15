## CRUD - microservice

### REST API

#### Query endpoints

GET		/cars?sort=<fieldName> get all (default sortBy: id)

GET 	/cars/{id} 

#### Command endpoints

POST	/cars/ with body of Ad model to create a new Car Ad
Sample request:

```json
{
  "id": 1,
  "title": "honda",
  "fuel": "Diesel",
  "price": 1000,
  "new": false,
  "firstRegistration": "2015-01-12"
}
```


PUT		/cars/{id} with body of Ad model - modifies existing Car
Sample request:

```json
{
  "id": 1,
  "title": "honda 2",
  "fuel": "Diesel",
  "price": 1000,
  "new": false,
  "firstRegistration": "2015-01-12"
}
```

DELETE	/cars/{id}

### Run with SBT

Use `sbt run` command

### Run with Docker Compose

#### Requirements
- Docker daemon needs to be available for SBT packager plugin

#### Build Docker image

First you need to build an image. In order to do that just run SBT commands to build a service image:

```bash
sbt stage
sbt docker:publishLocal
```

Now you can run the environment. There are two shell scripts to start and stop docker-compose environment which 
includes 
Postgres 
database
 as well

start script:
```bash
sh start.sh
``` 

stop script:
```bash
sh remove.sh
```
 
### Tests

#### Requirements
- Docker daemon needs to be available for SBT tests

There are unit and E2E tests (or Consumer based tests). All tests are triggered by `sbt test` command