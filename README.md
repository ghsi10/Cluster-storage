# Key Value Store

A simple distributed key value store using a REST API.

## Getting Started

```
Give examples

Storing a value:
    curl -X POST -H "Content-Type: application/json" -d '{"something": "blabla"}' http://<some_instance>/api/resource

Reading a value:
    curl -X GET http://<another_instance>/api/resource
```

## Deployment

The project can be used to build a docker image. 

First the project has to be built.
```
./mvnw package
```

Now we can build the Docker image.
```
docker build . --build-arg JAR_FILE=target/exercise-1.0.0.jar -t image_name
```

To run the image simply list the nodes in the cluster using the env variable "cluster", 
and the id of the particular node using the "id" env var.
It is vital that the id will be unique in order to keep the sharding algorithm correct,
and the cluster list must be in the same order in every instance.
```
docker run -d -p {exposed_port}:8080 --env id={node_id} --env cluster={host_name1}:{port},{host_name2}:{port} image_name
```

