package com.consoleconnect.kraken.operator.auth.model;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@ConditionalOnProperty(value = "app.security.login.mode", havingValue = "default")
@Service
public class DefaultUserLoginEnabled {}
