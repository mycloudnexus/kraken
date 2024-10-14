package com.consoleconnect.kraken.operator.auth.service;

import static org.mockito.Mockito.when;

import com.consoleconnect.kraken.operator.TestApplication;
import com.consoleconnect.kraken.operator.TestContextConstants;
import com.consoleconnect.kraken.operator.auth.dto.AuthRequest;
import com.consoleconnect.kraken.operator.auth.dto.CreateUserRequest;
import com.consoleconnect.kraken.operator.auth.enums.UserStateEnum;
import com.consoleconnect.kraken.operator.auth.model.AuthDataProperty;
import com.consoleconnect.kraken.operator.auth.model.User;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.test.AbstractIntegrationTest;
import com.consoleconnect.kraken.operator.test.MockIntegrationTest;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;

@ActiveProfiles("test-login-enabled")
@MockIntegrationTest
@ContextConfiguration(classes = {TestApplication.class})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Slf4j
class UserServiceTest extends AbstractIntegrationTest {
  @SpyBean private AuthDataProperty.Login login;

  @Autowired private UserService userService;

  @Order(1)
  @Test
  void givenUserNotExist_thenCreateUser_shouldOk() {

    String createdBy = UUID.randomUUID().toString();

    User user = createUser("test", "test@test.com", "USER", createdBy);
    Assertions.assertNotNull(user);
    Assertions.assertNotNull(user.getId());
    Assertions.assertEquals("test", user.getName());
    Assertions.assertEquals("test@test.com", user.getEmail());
    Assertions.assertEquals("USER", user.getRole());
    Assertions.assertEquals(createdBy, user.getCreatedBy());
    Assertions.assertEquals(UserStateEnum.ENABLED, user.getState());
    Assertions.assertNotNull(user.getCreatedAt());
  }

  private User createUser(String name, String email, String role, String createdBy) {
    return createUser(name, email, UUID.randomUUID().toString(), role, createdBy);
  }

  private User createUser(
      String name, String email, String password, String role, String createdBy) {
    CreateUserRequest createUserRequest = new CreateUserRequest();
    createUserRequest.setName(name);
    createUserRequest.setEmail(email);
    createUserRequest.setPassword(password);
    createUserRequest.setRole(role);

    return userService.create(createUserRequest, createdBy);
  }

  @BeforeEach
  void mockJwtTokenEncoder() {
    AuthDataProperty.JwtEncoderProperty encoderProperty = new AuthDataProperty.JwtEncoderProperty();
    encoderProperty.setIssuer("kraken-operator-sdk");
    encoderProperty.setKeyId("test-keyId");
    encoderProperty.setSecret(TestContextConstants.JWT_SECRET);
    when(login.getJwt()).thenReturn(encoderProperty);
  }

  @Order(2)
  @Test
  void givenUserExist_whenCreateUser_thenThrowException() {

    // create a user
    String email = UUID.randomUUID().toString() + "@test.com";
    createUser(UUID.randomUUID().toString(), email, "USER", UUID.randomUUID().toString());

    // create the same again, should throw exception
    KrakenException exception =
        Assertions.assertThrowsExactly(
            KrakenException.class,
            () ->
                createUser(
                    UUID.randomUUID().toString(), email, "USER", UUID.randomUUID().toString()));
    Assertions.assertEquals(400, exception.getCode());
    Assertions.assertEquals("User already exists", exception.getMessage());
  }

  @Test
  void givenCorrectUserId_whenGetUser_thenReturnOk() {
    String email = UUID.randomUUID().toString() + "@test.com";
    User user = createUser("test", email, "USER", UUID.randomUUID().toString());
    Assertions.assertNotNull(userService.findOne(user.getId()));
    Assertions.assertNotNull(userService.findOne(user.getEmail()));
  }

  @Test
  void givenWrongUserId_whenGetUser_thenThrowNotFound() {
    KrakenException exception =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> userService.findOne(UUID.randomUUID().toString()));
    Assertions.assertEquals(404, exception.getCode());
  }

  @Test
  void givenCorrectUserId_whenUpdateState_thenReturnOk() {
    String email = UUID.randomUUID().toString() + "@test.com";
    User user = createUser("test", email, "USER", UUID.randomUUID().toString());
    String updatedBy = UUID.randomUUID().toString();
    User updatedUser = userService.updateState(user.getId(), UserStateEnum.DISABLED, updatedBy);
    Assertions.assertEquals(UserStateEnum.DISABLED, updatedUser.getState());
    Assertions.assertEquals(updatedBy, updatedUser.getUpdatedBy());
    Assertions.assertNotNull(updatedUser.getUpdatedAt());

    updatedBy = UUID.randomUUID().toString();
    updatedUser = userService.updateState(user.getId(), UserStateEnum.ENABLED, updatedBy);
    Assertions.assertEquals(UserStateEnum.ENABLED, updatedUser.getState());
    Assertions.assertEquals(updatedBy, updatedUser.getUpdatedBy());
    Assertions.assertNotNull(updatedUser.getUpdatedAt());
  }

  @Test
  void givenWrongUserId_whenUpdateState_thenThrowNotFound() {
    KrakenException exception =
        Assertions.assertThrowsExactly(
            KrakenException.class,
            () ->
                userService.updateState(
                    UUID.randomUUID().toString(),
                    UserStateEnum.DISABLED,
                    UUID.randomUUID().toString()));
    Assertions.assertEquals(404, exception.getCode());
  }

  @Test
  void givenCorrectUserId_whenResetPassword_thenReturnOk() {
    String password = UUID.randomUUID().toString();
    String email = UUID.randomUUID().toString() + "@test.com";
    User user = createUser("test", email, password, "USER", UUID.randomUUID().toString());

    // login should work
    AuthRequest loginRequest = new AuthRequest();
    loginRequest.setEmail(email);
    loginRequest.setPassword(password);
    Assertions.assertNotNull(userService.login(loginRequest).getAccessToken());

    // reset password
    String updatedBy = UUID.randomUUID().toString();
    String newPassword = UUID.randomUUID().toString();
    User updatedUser = userService.resetPassword(user.getId(), newPassword, updatedBy);
    Assertions.assertNotNull(updatedUser);
    Assertions.assertNotNull(updatedUser.getUpdatedAt());
    Assertions.assertEquals(updatedBy, updatedUser.getUpdatedBy());

    // login via old password should not work
    KrakenException exception =
        Assertions.assertThrowsExactly(
            KrakenException.class, () -> userService.login(loginRequest));
    Assertions.assertEquals(401, exception.getCode());

    // login via new password should work
    loginRequest.setPassword(newPassword);
    Assertions.assertNotNull(userService.login(loginRequest).getAccessToken());
  }
}
