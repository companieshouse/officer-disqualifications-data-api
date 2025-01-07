# disqualified-officers-data-api
Handles CRUD functions for disqualified officers.

## Requirements

- [Java 21](https://www.oracle.com/java/technologies/downloads/#java21)
- [Maven](https://maven.apache.org/download.cgi)
- [Git](https://git-scm.com/downloads)

## Build

Common commands used for development and running locally can be found in the Makefile, each make target has a
description which can be listed by running `make help`.

```text
Target               Description
------               -----------
all                  Calls methods required to build a locally runnable version, typically the build target
build                Pull down any dependencies and compile code into an executable if required
clean                Reset repo to pre-build state (i.e. a clean checkout state)
package              Create a single versioned deployable package (i.e. jar, zip, tar, etc.). May be dependent on the 
build target being run before package
sonar                Run sonar scan
test                 Run all test-* targets (convenience method for developers)
test-unit            Run unit tests
```

## Building the docker image
```bash
mvn compile jib:dockerBuild
```

## Endpoints
| URL | Description |
| --- | ----------- |
| /healthcheck | Health check URL returns 200 if service is running |
| /disqualified-officers/natural/{officerId}/internal | Save or update a natural disqualified officer record |
| /disqualified-officers/corporate/{officerId}/internal | Save or update a corporate disqualified officer record |

## Terraform ECS

### What does this code do?

The code present in this repository is used to define and deploy a dockerised container in AWS ECS.
This is done by calling a [module](https://github.com/companieshouse/terraform-modules/tree/main/aws/ecs) from terraform-modules. Application specific attributes are injected and the service is then deployed using Terraform via the CICD platform 'Concourse'.


| Application specific attributes | Value                                                                                                                                                                                                                                                                            | Description                                         |
|:--------------------------------|:---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|:----------------------------------------------------|
| **ECS Cluster**                 | public-data                                                                                                                                                                                                                                                                      | ECS cluster (stack) the service belongs to          |
| **Load balancer**               | {env}-chs-apichgovuk <br> {env}-chs-apichgovuk-private                                                                                                                                                                                                                           | The load balancer that sits in front of the service |
| **Concourse pipeline**          | [Pipeline link](https://ci-platform.companieshouse.gov.uk/teams/team-development/pipelines/disqualified-officers-data-api) <br> [Pipeline code](https://github.com/companieshouse/ci-pipelines/blob/master/pipelines/ssplatform/team-development/disqualified-officers-data-api) | Concourse pipeline link in shared services          |

### Contributing
- Please refer to the [ECS Development and Infrastructure Documentation](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/4390649858/Copy+of+ECS+Development+and+Infrastructure+Documentation+Updated) for detailed information on the infrastructure being deployed.

### Testing
- Ensure the terraform runner local plan executes without issues. For information on terraform runners please see the [Terraform Runner Quickstart guide](https://companieshouse.atlassian.net/wiki/spaces/DEVOPS/pages/1694236886/Terraform+Runner+Quickstart).
- If you encounter any issues or have questions, reach out to the team on the **#platform** slack channel.

### Vault Configuration Updates
- Any secrets required for this service will be stored in Vault. For any updates to the Vault configuration, please consult with the **#platform** team and submit a workflow request.

### Useful Links
- [ECS service config dev repository](https://github.com/companieshouse/ecs-service-configs-dev)
- [ECS service config production repository](https://github.com/companieshouse/ecs-service-configs-production)
