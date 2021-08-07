package org.apache.flink.statefun.e2e.smoke;

import org.apache.flink.statefun.e2e.smoke.generated.Commands;
import org.apache.flink.statefun.e2e.smoke.generated.SourceCommand;
import org.apache.flink.statefun.e2e.smoke.generated.VerificationResult;
import org.apache.flink.statefun.sdk.TypeName;
import org.apache.flink.statefun.sdk.reqreply.generated.TypedValue;

final class TypeUtils {
  private TypeUtils() {}

  static final TypeName SOURCE_COMMANDS_TYPE =
      TypeName.parseFrom("statefun.smoke.e2e/sourceCommand");
  static final TypeName COMMANDS_TYPE = TypeName.parseFrom("statefun.smoke.e2e/commands");
  static final TypeName VERIFICATION_RESULT_TYPE =
      TypeName.parseFrom("statefun.smoke.e2e/verificationResult");

  static boolean isTypeOf(TypedValue value, TypeName type) {
    return value.getTypename().equals(type.canonicalTypenameString());
  }

  static TypedValue packCommands(Commands commands) {
    return TypedValue.newBuilder()
        .setTypename(COMMANDS_TYPE.canonicalTypenameString())
        .setHasValue(true)
        .setValue(commands.toByteString())
        .build();
  }

  static TypedValue packVerificationResult(VerificationResult verificationResult) {
    return TypedValue.newBuilder()
        .setTypename(VERIFICATION_RESULT_TYPE.canonicalTypenameString())
        .setHasValue(true)
        .setValue(verificationResult.toByteString())
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

  static Commands unpackCommands(TypedValue typedValue) {
    if (!isTypeOf(typedValue, COMMANDS_TYPE)) {
      throw new IllegalStateException("Unexpected TypedValue: " + typedValue);
    }
    try {
      return Commands.parseFrom(typedValue.getValue());
    } catch (Exception e) {
      throw new RuntimeException("Unable to parse Commands from TypedValue.", e);
    }
  }
}
