# Smoke E2E Test Framework

## Runtime Architecture

```
CommandFlinkSource -> StateFun Core -> SocketSink -> SimpleVerificationServer
(CommandGenerator)         ^
                           |
                           ˇ
                     Remote Functions
                   (CommandInterpreter)
```

## Framework Modules
### statefun-e2e-tests-common
The generic StatefulFunctionsAppContainers implementation which is used to spawn up the Flink cluster and other external services(such as remote functions or Kafka) as Docker containers.

### statefun-smoke-e2e-common
Testing utilities:
* SmokeRunner: facet class that organizes the runtime architecture.
* SimpleVerificationServer: takes the VerificationResult messages and performs the result verification.

### statefun-smoke-e2e-driver
The logic of the testing framework which generates a series of command and verification messages. 
The driver code is built into a self-contained jar that can be loaded and ran by the Flink StateFun cluster. 

### statefun-smoke-e2e-multilang-base
This contains a generic pom.xml that have the dependencies and driver jar downloaded for non-JVM SDKs to run Smoke E2E.

### statefun-smoke-e2e-multilang-harness
The harness test that can be ran directly in JUnit.

# Adding Smoke E2E for a Language SDK

For the steps below, take statefun-smoke-e2e-golang module and a reference. For static contents, you can also copy the files from there.

## Step 1: Add a new module
* Inherits the pom.xml from statefun-smoke-e2e-multilang-base as parent.
* Copy commands.proto into src/main/protobuf.
* Copy remote-module/module.yaml, Dockerfile, log4j.properties into src/test/resources.

## Step 2: Generate protobuf messages
Generate language specific protobuf message bindings from commands.proto.   

## Step 3: Implement CommandInterpreterFn and expose it as an HTTP endpoint
* Code up CommandInterpreterFn, which is a remote function implementation that performs state manipulation based on the messages received.
* Make sure the typename of remote function, messages, ingress/ingress are aligned with the driver's definition.  
* Wrap the CommandInterpreterFn as an HTTP endpoint.

## Step 4: Test via Harness
* Run Step 3's CommandInterpreterFn HTTP endpoint at localhost 8000 port.
* Run MultiLangSmokeHarnessTest under statefun-smoke-e2e-multilang-harness to test out your implementation.

## Step 5: Implement the SmokeVerificationE2E
* Implement the language SDK's version of SmokeVerificationE2E. One just need to focus on the resource preparation for launching language specific HTTP endpoint in the configureRemoteFunction method.
* Create Dockerfile.remote-function under src/test/resources, which takes the resources prepared by SmokeVerificationE2E and launches the CommandInterpreterFn HTTP endpoint in the container.