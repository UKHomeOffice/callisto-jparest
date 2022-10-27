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


## Using Filters

### Overview
JpaRest supports the use of filtering data during retrieval for certain get endpoints. These filters should be constructed in accordance with Spring Expression Language (SpEL) as they will then be converted into predicates used to query the database.

### Supported Endpoints
You can provide a filter as a request parameter for `GET` endpoints that return multiple resources. The filter expression should be assigned to a request parameter named `filter`.  
Examples of supported endpoints for providing a filter parameter are as follows:
- Getting multiple resources endpoint - `http://localhost:5000/resources/artists`
- Getting multiple related resources for a parent resource - `http://localhost:5000/resources/concerts/37e813a2-bb28-11ec-8422-0242ac120001/artists`


### Forming Expressions
Filter expressions can be formed through the use of logical operators or SpEL method invocation. The expression requires an operator and 1 or 2 operands depending on the type of operator.

#### Logical Operators
When forming a filter expression using logical operators the following conditions must be met:
- The left side operand must be a reference to a field on the entity. Literal values are not supported here.
- The right side operand can be a field reference or a literal value, however the data type of the right  
  side operand must match the data type of the left side operand.
- The following operators are supported: `==, !=, >=, >, <=, <`
- The operators can also be provided in the equivalent textual form such as: `eq, ne, ge, gt, le, lt`

An example of the filter request parameter using a logical operator is as follows:
`filter=id == 'dc27d5aa-4e7d-474e-98b6-ebf9aae9a471'`

#### Method References
When forming a filter expression using SpEL method invocation the following conditions must be met:
- Expressions can be formed using either of the following (case-insensitive) method references: `IN, BETWEEN`
- There should be at least 2 parameters passed to the method depending on which one it is.
- The first parameter (LHS) must be a reference to a field on the entity. Literal values are not supported here.
- The parameter/s (RHS) after the first must be literal value/s and their data types must match that of the first parameter.
  - For the  `BETWEEN` method reference there must be exactly two values after the first param.
  - For the `IN` method reference there must be at least 1 value after the first param.

An example of the filter request parameter using method references is as follows:
`filter=in(id, '27e813a2-bb28-11ec-8422-0242ac120001', '27e813a2-bb28-11ec-8422-0242ac120003')`


### Combining Conditions
There are a few additional logical operators that can be applied and also used to build a filter expression made up of multiple conditions.
- This can be applied with any of the following operators: `&&, ||, !`
- The operators can also be provided in the equivalent textual form such as: `and, or, not`

An example of the filter request parameter using additional logical operators is as follows:
`filter=!in(id, '27e813a2-bb28-11ec-8422-0242ac120001', '27e813a2-bb28-11ec-8422-0242ac120003')`


### Comparison Table
| Operator |Textual Version| LHS Operand Type| RHS Operand Type|  
|--|--|--|--|  
| == | eq| Field Reference | Field Ref / Literal |  
| != | ne| Field Reference | Field Ref / Literal |  
| >= | ge| Field Reference | Field Ref / Literal |  
| >  | gt| Field Reference | Field Ref / Literal |  
| <= | le| Field Reference | Field Ref / Literal |  
| <  | lt| Field Reference | Field Ref / Literal |  
| IN |   | Field Reference | Literal |  
| BETWEEN  | | Field Reference | Literal |  
| && | and| Expression | Expression |  
| \|\| | or| Expression  | Expression |  
| !| not|  | Expression  |  