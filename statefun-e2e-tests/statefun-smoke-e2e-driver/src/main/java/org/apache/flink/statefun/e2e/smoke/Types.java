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

import org.apache.flink.statefun.e2e.smoke.generated.SourceCommand;
import org.apache.flink.statefun.sdk.TypeName;
import org.apache.flink.statefun.sdk.reqreply.generated.TypedValue;

final class Types {
  private Types() {}

  public static final TypeName SOURCE_COMMANDS_TYPE =
      TypeName.parseFrom(Constants.NAMESPACE + "/sourceCommand");
  public static final TypeName VERIFICATION_RESULT_TYPE =
      TypeName.parseFrom(Constants.NAMESPACE + "/verificationResult");

  static boolean isTypeOf(TypedValue value, TypeName type) {
    return value.getTypename().equals(type.canonicalTypenameString());
  }

  static TypedValue packSourceCommand(SourceCommand sourceCommand) {
    return TypedValue.newBuilder()
        .setTypename(SOURCE_COMMANDS_TYPE.canonicalTypenameString())
        .setHasValue(true)
        .setValue(sourceCommand.toByteString())
        .build();
  }

  static SourceCommand unpackSourceCommand(TypedValue typedValue) {
    if (!isTypeOf(typedValue, SOURCE_COMMANDS_TYPE)) {
      throw new IllegalStateException("Unexpected TypedValue: " + typedValue);
    }
    try {
      return SourceCommand.parseFrom(typedValue.getValue());
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse SourceCommand from TypedValue.", e);
    }
  }
}
