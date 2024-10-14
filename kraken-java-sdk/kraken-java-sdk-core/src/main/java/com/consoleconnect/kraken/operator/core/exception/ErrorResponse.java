package com.consoleconnect.kraken.operator.core.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
  private String code;
  private String reason;
  private String message;
  private String referenceError;

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

    public static String defaultMsg(int code, String detailMessage) {
      if (ERROR_400_INVALID_BODY.getCode() == code) {
        if (null != detailMessage
            && detailMessage.contains("mapping")
            && detailMessage.contains("incomplete")) {
          return ERROR_400_IN_COMPLETED_MAPPING.getMsg();
        }
        return ERROR_400_INVALID_BODY.getMsg();
      } else if (ERROR_404_NOT_FOUND.getCode() == code) {
        return ERROR_404_NOT_FOUND.getMsg();
      } else if (ERROR_401_INVALID_CREDENTIALS.getCode() == code) {
        return ERROR_401_INVALID_CREDENTIALS.getMsg();
      } else if (ERROR_403_ACCESS_DENIED.getCode() == code) {
        return ERROR_403_ACCESS_DENIED.getMsg();
      } else if (ERROR_422_INVALID_FORMAT.getCode() == code) {
        return ERROR_422_INVALID_FORMAT.getMsg();
      } else {
        return ERROR_500_INTERNAL_ERROR.getMsg();
      }
    }
  }
}
