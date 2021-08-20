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
* SmokeRunner: facet class that organizes the Smoke E2E runtime architecture.
* SimpleVerificationServer: takes the VerificationResult messages and performs the result verification.

### statefun-smoke-e2e-driver
The core logic of the testing framework which generates a series of command and verification messages via CommandGenerator.
The driver code is built into a self-contained jar that can be loaded and ran by the Flink StateFun cluster. 

### statefun-smoke-e2e-multilang-base
This contains a generic pom.xml that have the dependencies and driver jar downloaded for non-JVM SDKs to run Smoke E2E.

### statefun-smoke-e2e-multilang-harness
The harness test that can be ran directly by JUnit. Noted that to run the harness test correctly, one should have a remote function up and running at localhost 8000 port for the harness to interact with.

# Adding Smoke E2E for a Language SDK
For the steps below, take statefun-smoke-e2e-golang module as an implementation reference. You can copy the static config files from there as well.

## Step 1: Add a new module
* Make statefun-smoke-e2e-multilang-base as parent of new module's pom.xml.
```
    <parent>
        <groupId>org.apache.flink</groupId>
        <artifactId>statefun-smoke-e2e-multilang-base</artifactId>
        <version>3.1-SNAPSHOT</version>
        <relativePath>../statefun-smoke-e2e-multilang-base/pom.xml</relativePath>
    </parent>
```
* Copy commands.proto into src/main/protobuf.
* Copy remote-module/module.yaml, Dockerfile, log4j.properties into src/test/resources.

## Step 2: Generate protobuf messages
Generate language specific protobuf message bindings using the commands.proto definition.

## Step 3: Implement CommandInterpreterFn and expose it as an HTTP endpoint
* Code up CommandInterpreterFn using the language SDK. It's a remote function implementation that performs state manipulation based on the commands received. One can mimic the logic by looking into other language SDK implementation.
* Make sure the typename of remote function, messages, ingress/ingress are all aligned with the driver's definition.
* Wrap the CommandInterpreterFn as an HTTP endpoint using a simple web container.

## Step 4: Test via Harness
* Run Step 3's CommandInterpreterFn HTTP endpoint at localhost 8000 port.
* Run MultiLangSmokeHarnessTest under statefun-smoke-e2e-multilang-harness to test out your implementation.

## Step 5: Implement the SmokeVerificationE2E
* Implement the language SDK's version of SmokeVerificationE2E. One just need to focus on preparing resources for launching language specific HTTP endpoint in the configureRemoteFunction method.
* Create Dockerfile.remote-function under src/test/resources, which takes the resources prepared by SmokeVerificationE2E and launches the CommandInterpreterFn HTTP endpoint in the container.
* The SmokeRunner orchestrates the Smoke E2E runtime by doing the following:
  * Launch the Flink StateFun cluster, which is defined by StatefulFunctionsAppContainers.Builder.
  * Launch SimpleVerificationServer that collects the verification results.
  * Wait until verification succeed and exit out.