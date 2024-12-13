package com.consoleconnect.kraken.operator.core.exception;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private String code;
  private String reason;
  private String message;
  private String referenceError;
  private String propertyPath;

  @Getter
  public enum ErrorMapping {
    ERROR_400_INVALID_BODY(400, "invalidBody"),
    ERROR_400_INVALID_QUERY(400, "invalidQuery"),
    ERROR_400_IN_COMPLETED_MAPPING(400, "incompleteMapping"),
    ERROR_400_MISSING_QUERY_PARAM(400, "missingQueryParameter"),
    ERROR_400_MISSING_QUERY_VALUE(400, "missingQueryValue"),
    ERROR_401_MISSING_CREDENTIALS(401, "missingCredentials"),
    ERROR_401_INVALID_CREDENTIALS(401, "invalidCredentials"),
    ERROR_403_ACCESS_DENIED(403, "accessDenied"),
    ERROR_403_FORBIDDEN_REQUESTER(403, "forbiddenRequester"),
    ERROR_403_TOO_MANY_USERS(403, "tooManyUsers"),
    ERROR_422_MISSING_PROPERTY(422, "missingProperty"),
    ERROR_422_INVALID_VALUE(422, "invalidValue"),
    ERROR_422_INVALID_FORMAT(422, "invalidFormat"),
    ERROR_422_REFERENCE_NOT_FOUND(422, "referenceNotFound"),
    ERROR_422_UNEXPECTED_PROPERTY(422, "unexpectedProperty"),
    ERROR_422_TOO_MANY_RECORDS(422, "tooManyRecords"),
    ERROR_422_OTHER_ISSUE(422, "otherIssue"),
    ERROR_404_NOT_FOUND(404, "notFound"),
    ERROR_500_INTERNAL_ERROR(500, "internalError"),
    ERROR_501_NOT_IMPLEMENTED(501, "notImplemented");

    private final int code;
    private final String msg;

    ErrorMapping(int code, String msg) {
      this.code = code;
      this.msg = msg;
    }

    private static final Map<Integer, Function<Throwable, String>> errorMap =
        Map.of(
            ERROR_400_INVALID_BODY.getCode(),
                throwable -> {
                  if (StringUtils.isNotBlank(throwable.getMessage())
                      && throwable.getMessage().contains("mapping")
                      && throwable.getMessage().contains("incomplete")) {
                    return ERROR_400_IN_COMPLETED_MAPPING.getMsg();
                  }
                  return ERROR_400_INVALID_BODY.getMsg();
                },
            ERROR_404_NOT_FOUND.getCode(), throwable -> ERROR_404_NOT_FOUND.getMsg(),
            ERROR_401_INVALID_CREDENTIALS.getCode(),
                throwable -> ERROR_401_INVALID_CREDENTIALS.getMsg(),
            ERROR_403_ACCESS_DENIED.getCode(), throwable -> ERROR_403_ACCESS_DENIED.getMsg(),
            ERROR_422_INVALID_FORMAT.getCode(), ErrorMapping::process422);

    public static String defaultMsg(int code, Throwable throwable) {
      return errorMap
          .getOrDefault(code, item -> ERROR_500_INTERNAL_ERROR.getMsg())
          .apply(throwable);
    }

    public static String process422(Throwable throwable) {
      Predicate<String> isMissingProperty =
          message -> message.contains(ERROR_422_MISSING_PROPERTY.getMsg());
      Predicate<String> isInvalidValue =
          message -> message.contains(ERROR_422_INVALID_VALUE.getMsg());

      return Optional.ofNullable(throwable)
          .flatMap(t -> Optional.ofNullable(t.getMessage()))
          .filter(isMissingProperty.or(isInvalidValue))
          .orElseGet(
              () -> {
                if (throwable == null) {
                  return ERROR_422_INVALID_FORMAT.getMsg();
                }
                return Optional.ofNullable(throwable.getCause())
                    .flatMap(cause -> Optional.ofNullable(cause.getMessage()))
                    .filter(isMissingProperty.or(isInvalidValue))
                    .orElseGet(ERROR_422_INVALID_FORMAT::getMsg);
              });
    }
  }
}
