# JPA REST 
The goal of the project is to provide a easy way to expose resources (JPA entity) over HTTP.

This take a JPA entity and exposes it in an opinionated fashion as a RESTful API that allows CRUD capability over your entities and
associations. It also exposes a filter parameter for querying resources.

## Features

* Exposes a REST API for your domain model.
* Supports pagination 
* Allows to dynamically filter resources.
* OpenAPI documentation for exposed endpoints

## Getting Started

The project can be run in VSCode by running the `Launch Demo` configuration.

If using VSCode, please not that calling lifecycle commands on projects doesn't work correctly for a multi module project.
Instead try calling mvnw with the `-am` option and specify the target module with the `-pl` option.

```bash
$ mvnw package -pl demo -am   
```

## Building from Source

JPA Rest can be easily built with the maven wrapper.
You also need JDK 1.7.

```bash
 $ ./mvnw clean install
```