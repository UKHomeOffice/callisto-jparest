# JPA REST

The goal of the project is to provide an easy way to expose resources (JPA entity) over HTTP.

This takes a JPA entity and exposes it in an opinionated fashion as a RESTful API that allows CRUD capability over your entities and
associations. It also exposes a filter parameter for querying resources.

Features:

- Exposes a REST API for your domain model.
- Supports pagination
- Allows to dynamically filter resources.
- OpenAPI documentation for exposed endpoints

## The reason for this library

The decision to use a home-made library like this incurs maintenance overheads, and the reasons for taking this on must be well considered.

### What is JPA REST designed to give us?

- A vast reduction in boilerplate code required for writing CRUD APIs.
- A place to implement our authorization logic at a data level
- Standard response format for happy and error paths
- (reasonably) flexible resource querying without the need for native SQL

### What is JPA REST _not_ designed to give us?

[todo]

### Choices that JPA REST makes for our services

[todo]

### “Why are we not just using Spring Data JPA?”

Spring Data JPA was considered as alternative. JPA REST brings the following comparable advantages:

- There’s no need to write a repository for every entity, reducing boilerplate
- JPA REST provides a place to make technical decisions that we can consistently apply to all of our Callisto services.

## Getting Started

The project can be run in VSCode by running the `Launch Demo` configuration.

### If using VSCode

Please note that calling lifecycle commands on projects doesn't work correctly for a multi module project.
Instead try calling mvnw with the `-am` option and specify the target module with the `-pl` option.

```bash
$ mvnw package -pl demo -am
```

#### VS Code extensions

- Extension Pack for Java
- Lombok Annotations Support

#### JDK configuration

If experiencing build errors check your JDK is configured correctly:

Command + , to open settings.

Search for "java runtime" and click the "Edit in settings.json" link under Java › Configuration: Runtimes.

Enter the configuration similar to below. Ensure you specify the correct version name and path to your installed JDK.

```
"java.configuration.runtimes": [

    {
      "name": "JavaSE-17",
      "path": "/Library/Java/JavaVirtualMachines/openjdk.jdk/Contents/Home"
    }
  ]
```

## Building from Source

JPA Rest can be easily built with the maven wrapper.
You also need JDK 17.

```bash
 $ ./mvnw clean install
```

## Bumping Versions

To update the parent pom version globally throughout the project run the following command.

```bash
  $ mvn versions:set -DnewVersion={version} -f pom.xml
```

When pushing a branch jparest will automatically deploy a snapshot image to artifactory. The naming convention for this
is as follows

```bash
{Version-Number}-{Jira-Reference}-SNAPSHOT
```

## Merging into Main

Once a PR is merged into the main branch the snapshot suffix will be deleted from the version.
The assigned version number will be used to create a version release in artifactory and a Github tag for backup.
