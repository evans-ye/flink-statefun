/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.flink.statefun.e2e.smoke;

import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.flink.statefun.e2e.common.StatefulFunctionsAppContainers;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.images.builder.ImageFromDockerfile;

public class SmokeVerificationJavaE2E {

  private static final Logger LOG = LoggerFactory.getLogger(SmokeVerificationJavaE2E.class);
  private static final String REMOTE_FUNCTION_HOST = "remote-function";
  private static final int NUM_WORKERS = 2;

  @Test(timeout = 1_000 * 60 * 10)
  public void runWith() throws Throwable {
    ModuleParameters parameters = new ModuleParameters();
    parameters.setNumberOfFunctionInstances(128);
    parameters.setMessageCount(100_000);
    parameters.setMaxFailures(1);

    Path targetDirPath = Paths.get(System.getProperty("user.dir") + "/target/");
    ImageFromDockerfile remoteFunctionImage =
        new ImageFromDockerfile("remote-function")
            .withFileFromClasspath("Dockerfile", "Dockerfile.remote-function")
            .withFileFromPath(".", targetDirPath);

    GenericContainer<?> remoteFunction =
        new GenericContainer<>(remoteFunctionImage)
            .withNetworkAliases(REMOTE_FUNCTION_HOST)
            .withLogConsumer(new Slf4jLogConsumer(LOG))
            .withEnv(
                "NUM_FN_INSTANCES", Integer.toString(parameters.getNumberOfFunctionInstances()));

    StatefulFunctionsAppContainers.Builder builder =
        StatefulFunctionsAppContainers.builder("smoke-e2e-java", NUM_WORKERS)
            .dependsOn(remoteFunction)
            .withBuildContextFileFromClasspath("remote-module", "/remote-module/");

    SmokeRunner.run(parameters, builder);
  }
}
