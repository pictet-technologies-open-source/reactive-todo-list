[![CircleCI](https://circleci.com/gh/pictet-technologies-open-source/reactive-todo-list/tree/main.svg?style=shield&circle-token=90b39e6ac2420e2e4e4991cc34325c1ca74fa263)](https://circleci.com/gh/pictet-technologies-open-source/reactive-todo-list)
[![CodeFactor](https://www.codefactor.io/repository/github/pictet-technologies-open-source/reactive-todo-list/badge?s=88d8b4d1338a9d7d41b62e825d1f2d1a61fe6ee4)](https://www.codefactor.io/repository/github/pictet-technologies-open-source/reactive-todo-list)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)


# Reactive Todo List 

This project aims to explain how to build a fully reactive application with Spring Boot, Spring Webflux, Angular and MongoDB.
It demonstrates how to deal with concurrent modifications using optimistic locking and Server Sent Events.

<p align="center">
<img width="60%" height="60%" src="https://lh3.googleusercontent.com/sqUKGUFVt4qwAAEqTJk_eycuj00imob1MtSVMFQ-6H_bPXEQmD84xw8TdOzqDxUD16ToclYrVnxRot4IsUq8bRFcUrOl_MA3yoCmSDRWWVB4UfmVv7dG7H_PJIqyWfqTrkkYJUemi_E=w1920-h1080">
</p>

### Project structure


Folder                    | Description                                                  
--------------------------|--------------------------------------------------------------
docker                    | Contains the docker-compose.yml used to setup the application 
todo-list-application     | Spring boot application (back-end) 
todo-list-ui              | Angular application (front-end)
 


### Local environment

#### Build the application

In order to build the application you need to have the following softwares installed:
- open JDK or oracle JDK >= 8
- docker & docker compose
- npm

```
$ build.sh
```

#### Start the application

```
$ start.sh
```

Once the application is started you can access it using the following links : 

Name                      | Link                                                  
--------------------------|--------------------------------------------------------------
UI                        | http://localhost:8080
Swagger UI                | http://localhost:8080/swagger-ui/#/item-controller
Mongo Express             | http://0.0.0.0:8081/


If you only want to start mongoDb, execute the following command

```
$ start.sh -mongo-only
```

#### Stop the application

```
$ stop.sh
```


### Production environment

#### Build the application

You need to have docker installed to build the application.

Ensure that the URL of the application has been correctly configured in the following file.

```
todo-list-ui/src/app/environments/environment.prod.ts
```

Then build the application in production mode.

```
$ mvnw clean install -Pprod
```

#### Deploy the application to Heroku

You need to have the Heroku client installed to deploy the application (see <a href="https://devcenter.heroku.com/articles/heroku-cli">Heroku client</a>)

After having built the application in production mode, follow these steps:


##### Go the the todo-list-application folder

```
$ cd todo-list-application
```

##### Log in to heroku

```
$ heroku login
```

##### Log in to Continer registry

```
$ heroku container:login
```

##### Push your Docker-based app

```
$ heroku container:push web -a application_name_in_heroku
```

##### Deploy the changes

```
$ heroku container:release web -a application_name_in_heroku
```
