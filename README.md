# JPA REST

The goal of the project is to provide an easy way to expose resources (JPA entity) over HTTP.

This takes a JPA entity and exposes it in an opinionated fashion as a RESTful API that allows CRUD capability over your entities and
associations. It also exposes a filter parameter for querying resources.

## Features

- Exposes a REST API for your domain model.
- Supports pagination
- Allows to dynamically filter resources.
- OpenAPI documentation for exposed endpoints

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
JPA Rest has a shell script which will automatically update the version number.

Run the following command in the root directory.

./updateVersion.sh {version-number}

When pushing a branch jparest will automatically deploy a snapshot image to artifactory. The naming convention for this
is as follows 

{version-number}-{Jira-Reference}-SNAPSHOT

## Merging into main
Once a PR is merged into the main branch the snapshot suffix will be deleted from the version.
The assigned version number will be used to create a version release in artifactory and a Github tag for backup.
