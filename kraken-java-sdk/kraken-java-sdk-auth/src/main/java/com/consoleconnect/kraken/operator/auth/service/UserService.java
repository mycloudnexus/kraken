package com.consoleconnect.kraken.operator.auth.service;

import com.consoleconnect.kraken.operator.auth.dto.*;
import com.consoleconnect.kraken.operator.auth.entity.UserEntity;
import com.consoleconnect.kraken.operator.auth.enums.AuthGrantTypeEnum;
import com.consoleconnect.kraken.operator.auth.enums.UserRoleEnum;
import com.consoleconnect.kraken.operator.auth.enums.UserStateEnum;
import com.consoleconnect.kraken.operator.auth.enums.UserTokenTypeEnum;
import com.consoleconnect.kraken.operator.auth.jwt.JwtEncoderToolkit;
import com.consoleconnect.kraken.operator.auth.mapper.UserMapper;
import com.consoleconnect.kraken.operator.auth.model.*;
import com.consoleconnect.kraken.operator.auth.repo.UserRepository;
import com.consoleconnect.kraken.operator.auth.security.UserContext;
import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.DateTime;
import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import jakarta.annotation.PostConstruct;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@ConditionalOnBean(UserLoginEnabled.class)
@AllArgsConstructor
@Service
@Slf4j
public class UserService {

  private final AuthDataProperty.Login loginConfig;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final UserTokenService userTokenService;

  @PostConstruct
  public void initialize() {
    log.info("Initializing user data");
    if (userRepository.count() > 0) {
      log.info("User data already initialized");
      return;
    }

    if (loginConfig.getUserList() == null || loginConfig.getUserList().isEmpty()) {
      log.info("No user data to initialize");
      return;
    }
    for (UserEntity userEntity : loginConfig.getUserList()) {
      if (userEntity.getName() == null) {
        userEntity.setName(userEntity.getEmail());
      }
      if (userEntity.getRole() == null) {
        userEntity.setRole(UserRoleEnum.USER.name());
      }
      if (userEntity.getState() == null) {
        userEntity.setState(UserStateEnum.ENABLED);
      }

      if (userEntity.getPassword() == null) {
        String password = UUID.randomUUID().toString();
        log.warn(
            "Default password generated for user {},password:{}", userEntity.getEmail(), password);
        userEntity.setPassword(passwordEncoder.encode(password));
      }
    }

    log.info("Creating {} users", loginConfig.getUserList().size());
    userRepository.saveAll(loginConfig.getUserList());
    log.info("User data initialized");
  }

  public User create(CreateUserRequest request, String createdBy) {
    log.info("Creating user {}", request.getEmail());

    userRepository
        .findOneByEmail(request.getEmail())
        .ifPresent(
            userEntity -> {
              log.warn("User {} already exists", userEntity.getId());
              throw KrakenException.badRequest("User already exists");
            });

    UserEntity userEntity = UserMapper.INSTANCE.toEntity(request);
    userEntity.setPassword(passwordEncoder.encode(request.getPassword()));
    userEntity.setState(UserStateEnum.ENABLED);
    userEntity.setCreatedAt(DateTime.nowInUTC());
    userEntity.setCreatedBy(createdBy);
    userEntity = userRepository.save(userEntity);
    log.info("User {} created", userEntity.getId());
    return UserMapper.INSTANCE.toUser(userEntity);
  }

  public Paging<User> search(String q, PageRequest pageRequest) {
    log.info("Searching users, q:{}, pageRequest:{}", q, pageRequest);
    Page<UserEntity> userEntityPage = userRepository.search(q, pageRequest);
    log.info("Users found:{}", userEntityPage.getTotalElements());
    return PagingHelper.toPaging(userEntityPage, UserMapper.INSTANCE::toUser);
  }

  public User findOne(String id) {
    User user = UserMapper.INSTANCE.toUser(this.findOneByIdOrEmail(id));

    List<UserToken> tokens =
        userTokenService
            .search(
                id,
                false,
                UserTokenTypeEnum.REFRESH_TOKEN,
                PageRequest.of(PagingHelper.DEFAULT_PAGE, PagingHelper.DEFAULT_SIZE))
            .getData();
    user.setTokens(tokens);

    return user;
  }

  public void revokeUserTokens(String userId, String requestedBy) {
    log.info("Revoking all refresh tokens for user {}, requestedBy:{}", userId, requestedBy);
    userTokenService.revokeAllTokensByUserId(userId, UserTokenTypeEnum.REFRESH_TOKEN, requestedBy);
    log.info("Revoked all refresh tokens for user {}", userId);
  }

  public User update(String userId, UpdateUserRequest request, String updatedBy) {
    log.info("Updating user {}, request:{}, updatedBy:{}", userId, request, updatedBy);
    UserEntity userEntity = this.findEnabledUser(userId);
    if (request.getName() != null) userEntity.setName(request.getName());
    if (request.getRole() != null) userEntity.setRole(request.getRole());
    if (request.getEmail() != null && !request.getEmail().equalsIgnoreCase(userEntity.getEmail())) {
      Optional<UserEntity> optionalUserEntity = userRepository.findOneByEmail(request.getEmail());
      if (optionalUserEntity.isPresent()) {
        throw KrakenException.badRequest("Email already exists");
      }
      userEntity.setEmail(request.getEmail());
    }
    userEntity.setUpdatedBy(updatedBy);
    userEntity = userRepository.save(userEntity);

    return UserMapper.INSTANCE.toUser(userEntity);
  }

  public User resetPassword(String userId, String newPassword, String updatedBy) {
    log.info("Resetting password for user {}, updatedBy:{}", userId, updatedBy);
    UserEntity userEntity = this.findEnabledUser(userId);
    userEntity.setPassword(passwordEncoder.encode(newPassword));
    userEntity.setUpdatedAt(DateTime.nowInUTC());
    userEntity.setUpdatedBy(updatedBy);
    userEntity = userRepository.save(userEntity);
    log.info("Password reset for user {}", userId);
    return UserMapper.INSTANCE.toUser(userEntity);
  }

  public User updateState(String userId, UserStateEnum state, String updatedBy) {
    log.info("Updating a user's state {},new state:{}, updatedBy:{}", userId, state, updatedBy);
    UserEntity userEntity = this.findOneByIdOrEmail(userId);
    userEntity.setState(state);
    userEntity.setUpdatedAt(DateTime.nowInUTC());
    userEntity.setUpdatedBy(updatedBy);
    userEntity = this.userRepository.save(userEntity);
    log.info("User {} state updated", userId);
    return UserMapper.INSTANCE.toUser(userEntity);
  }

  public UserEntity findOneByIdOrEmail(String idOrEmail) {
    Optional<UserEntity> optionalUserEntity = userRepository.findOneByEmail(idOrEmail);
    if (optionalUserEntity.isEmpty()) {
      try {
        UUID uuid = UUID.fromString(idOrEmail);
        optionalUserEntity = userRepository.findById(uuid);
      } catch (Exception ex) {
        log.warn("Invalid user id or email {}", idOrEmail);
      }
    }
    return optionalUserEntity.orElseThrow(() -> KrakenException.notFound("User not found"));
  }

  private UserEntity findEnabledUser(String idOrEmail) {
    UserEntity userEntity = findOneByIdOrEmail(idOrEmail);
    if (userEntity.getState() != UserStateEnum.ENABLED) {
      log.warn("User {} is not enabled", userEntity.getId());
      throw KrakenException.notFound("User is not enabled");
    }
    return userEntity;
  }

  public AuthResponse onAuth(AuthRequest request) {
    if (request.getGrantType() == AuthGrantTypeEnum.USERNAME_PASSWORD) {
      return login(request);
    } else if (request.getGrantType() == AuthGrantTypeEnum.REFRESH_TOKEN) {
      return renewAccessToken(request.getRefreshToken());
    }
    throw KrakenException.badRequest("Invalid grant type");
  }

  public AuthResponse login(AuthRequest loginRequest) {
    log.info("Logging in user {}", loginRequest.getEmail());
    UserEntity userEntity = null;

    try {
      userEntity = findEnabledUser(loginRequest.getEmail());
    } catch (Exception e) {
      throw KrakenException.unauthorized("User not found");
    }

    log.info("User {} found", userEntity.getId());

    if (!passwordEncoder.matches(loginRequest.getPassword(), userEntity.getPassword())) {
      log.warn("Invalid password, user {}", userEntity.getId());
      throw KrakenException.unauthorized("Invalid password");
    }

    return generateLoginResponse(loginConfig, userEntity, null);
  }

  public AuthResponse renewAccessToken(String refreshToken) {
    log.info("Renewing access token");
    if (!loginConfig.getRefreshToken().isEnabled()) {
      log.error("Refresh token is not enabled");
      throw KrakenException.badRequest("Refresh token is not enabled");
    }
    if (refreshToken == null || refreshToken.trim().isEmpty()) {
      throw KrakenException.badRequest("Refresh token is required");
    }

    UserToken userToken =
        userTokenService
            .findOneByToken(refreshToken)
            .orElseThrow(() -> KrakenException.badRequest("Invalid refresh token"));
    UserEntity userEntity = findEnabledUser(userToken.getUserId());
    return generateLoginResponse(loginConfig, userEntity, userToken.getId());
  }

  private AuthResponse generateLoginResponse(
      AuthDataProperty.Login loginConfig, UserEntity userEntity, String tokenId) {
    JwtToken accessToken = generateAccessToken(userEntity, loginConfig);
    AuthResponse response = new AuthResponse();
    response.setId(userEntity.getId().toString());
    response.setAccessToken(accessToken.getToken());
    response.setTokenType(accessToken.getTokenType());
    response.setExpiresIn(accessToken.getExpiresIn());
    if (loginConfig.getRefreshToken().isEnabled()) {
      JwtToken jwtRefreshToken =
          generateRefreshToken(userEntity, tokenId, loginConfig.getRefreshToken());
      response.setRefreshToken(jwtRefreshToken.getToken());
      response.setRefreshTokenExpiresIn(jwtRefreshToken.getExpiresIn());
    }

    return response;
  }

  private JwtToken generateAccessToken(UserEntity userEntity, AuthDataProperty.Login loginConfig) {
    log.info("User {} logged in, generating access token", userEntity.getId());
    UUID uuid = userEntity.getId();
    String userId = uuid.toString();
    Map<String, Object> claims = new HashMap<>();
    claims.put(UserContext.TOKEN_CLAIM_ROLES, List.of(userEntity.getRole()));
    long tokenExpiredInSeconds = loginConfig.getTokenExpiresInSeconds();
    final String token =
        JwtEncoderToolkit.get(loginConfig.getJwt())
            .generateToken(userId, claims, tokenExpiredInSeconds);
    JwtToken jwtToken = new JwtToken();
    jwtToken.setToken(token);
    jwtToken.setExpiresIn(tokenExpiredInSeconds);
    jwtToken.setTokenType(UserContext.AUTHORIZATION_HEADER_PREFIX.trim());
    log.info(
        "User {} logged in, access token generated,expiredInSeconds:{}",
        userEntity.getId(),
        tokenExpiredInSeconds);
    return jwtToken;
  }

  private JwtToken generateRefreshToken(
      UserEntity userEntity, String tokenId, AuthDataProperty.RefreshToken refreshTokenConfig) {
    log.info("User {} logged in, generating refresh token", userEntity.getId());
    UUID uuid = userEntity.getId();
    long tokenExpiresInSeconds = refreshTokenConfig.getTokenExpiresInSeconds();
    final String token =
        Base64.getEncoder().encodeToString(UUID.randomUUID().toString().getBytes());
    JwtToken jwtToken = new JwtToken();
    jwtToken.setToken(token);
    jwtToken.setExpiresIn(tokenExpiresInSeconds);
    jwtToken.setTokenType("API_KEY");
    log.info(
        "User {} logged in, refresh token generated,expiresInSeconds:{}",
        userEntity.getId(),
        tokenExpiresInSeconds);

    if (tokenId == null) {
      CreateUserTokenRequest request = new CreateUserTokenRequest();
      request.setUserId(uuid.toString());
      request.setName("refresh_token");
      request.setToken(token);
      request.setTokenType(UserTokenTypeEnum.REFRESH_TOKEN);
      request.setExpiresInSeconds(tokenExpiresInSeconds);
      request.setMaxLifeEndInSeconds(refreshTokenConfig.getMaxLifeEndInSeconds());
      UserToken userToken = userTokenService.createToken(request, uuid.toString());
      log.info("Refresh token created, id:{}", userToken.getId());
    } else {
      UpdateUserTokenRequest request = new UpdateUserTokenRequest();
      request.setToken(token);
      request.setExpiresInSeconds(tokenExpiresInSeconds);
      UserToken userToken = userTokenService.updateToken(tokenId, request, uuid.toString());
      log.info("Refresh token updated, id:{}", userToken.getId());
    }
    return jwtToken;
  }
}
