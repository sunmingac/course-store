This project will use only type-level, best and latest library to build services.
* The stack consists of
* Scala main language
* Cats for pure functional
* http4s for http server
* postgres for relational db
* skunk for db connector
* munit for testing framework
* docker/k8s for deployment to google gcp

Course Shopping

https://github.com/tpolecat/skunk

## TODO
* Domain modeling
* Persistnece layer
* Api layer -<>
* jwt based token ...
* Graphql
* dockerise
* GCP


## How to Run

`sbt core/run`

*Press ENTER to shutdown server*


Swagger Documentation
http://localhost:8080/docs