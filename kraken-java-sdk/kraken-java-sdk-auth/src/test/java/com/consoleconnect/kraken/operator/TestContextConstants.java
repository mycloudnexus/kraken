package com.consoleconnect.kraken.operator;

public class TestContextConstants {
  private TestContextConstants() {}

  public static final String ADMIN_LOGIN_USERNAME = "admin";
  public static final String LOGIN_USERNAME = "test";
  public static final String LOGIN_PASSWORD = "password";

  public static final String JWT_SECRET = "MjRhMjNhNGYtMWUzMS00ZTY4LTlhYzgtMzY5NDcwYzNjNDE4";

  public static final String JWT_RSA_PRIVATE_KEY =
      """
      MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCZBn0ljiNmDObqDfTZq/VYAqXd
      KH3cR1W72ku7QQyx8CPaN0/IrboSPPZXQSlss/1zAoujD9bQ12ZIBwicAE0THrLsg/DI1wb4fTVq
      kq2z2FdJknJgxTpnwCrQFwg83/LH27iRJf80VhmwaDpfieooxro+Rmw0zfuP7cMR0mSiGgojzcl8
      GneQMHawplHtwZx4GsjtbfaSSQoNOjS8w1YtpeJ1N825Bj44B44v5kbVm4l0xDBlKvBHYFYk6M3V
      kPMnx7Awf8STySlb2IG4RFJzbJgTBJb5sdZMrHoC1JnG8KKm1U9Y5ypITRgE1ndZfUJ2/yPc4JXN
      Z6oScfBGaIq/AgMBAAECggEADO0fZSy8kMJGPz8uj7SW5PhUSnpBJ3pfI58tBoRYLi+615QUiT8j
      mKbNqFE4zpXlsvFkGkIcQ0fuu+bcM1FTMWpGcvQyQsTnMdZhzL7XLXn45Z4ZS1Ndu6Xf9/P6HipM
      t9U3ogszo5giU2R6idd8kDU5RklsmolL1rye3cKH/alT7pAF4gdOIlRElPFQsPefUm4eF8/E70fs
      wY9J4nYgygp7AJLXItqKDnMJhTXK0NdHybyDhWcWDg8c5na4RXrTsmiG0VBJCYcR7c/m7gZRjraU
      nEtLBD5KpPd9B47U9VZ8wRyAY/h+fE2dGISqORT+3fsEsZqw+gchP36m2XwnAQKBgQDTmV0yTVCX
      gP1nbFzXriWUgTDT+Mbne44T/bt2nMY5P7lf0Ar8qQJV+FENZiBkiQ7q3X5iOVKqOKv44UdkOFy9
      TtUOwgktmWSc26WGHRg6snAW+JTRS7zSwpF5r+16HQRIADZhAm5COWJyX13+hoc5E76ZAiYeJFp5
      brCNBeNWiwKBgQC5IqxQzIOUJUjvxAOq+a0I+rtR6CxjesKLFB4HSubEIqylpgPEOKGlPl4WmnPB
      b/eA8tHFNDug00ZayWHXSu76C28+kmOdLutq+u9vH70hI8Eo7JF4A1pt7qGRYqG8U5FAZ015agnP
      a5UgLwH2tWbfE6CajFQ+eJWMFDj0ogvXHQKBgQDF5KzQmpZA2xYjX6kE3Y2v8ZMk64m6fEdIUwi7
      yatpZzuLbBqaacYReQb0rQqme7hD15qXalzxbccIIEiQZlX1hoCkKLxEtSzvtkx7vUInwzIpiZpE
      k7yor+c2E17Z0cFrRSeWWpubu+diZ+aUYGSe8ORUXTog/obPAsKDYXpYOQKBgQCyP5L+3FnXOirG
      RshtRqT2H0pZaxvJz/kdJQpgZRpYPzZQ6s13kDr6OVknlB/dX6tCKQgVQdwjVSfI41njio8aigXa
      jeGBBhq9zsrXCAz1qlIgz9yjWz8m1voSW49zHlacVpm+S30UivVyni/a0/8uytv30SKPX1RFIXdQ
      +fBCdQKBgGBNsO6hJnRy2a/BD9Y4ZY4mhD2DQrw5xC9hX4OU5SlqH3fC2Fmrgr+e8NAn0z+n9qh0
      sH/FxHv5CAKEo2xrCH2meeKzdJz3QFNKtS/yrBVGGB8HuNqHnvjbf9o5igNjpX5LZx3wUm7RpjoP
      ag3+XOoRP2e4aUQz50ym+QW5nwCJ
      """;

  public static final String JWT_RSA_PUBLIC_KEY =
      """
      MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAmQZ9JY4jZgzm6g302av1WAKl3Sh93EdV
      u9pLu0EMsfAj2jdPyK26Ejz2V0EpbLP9cwKLow/W0NdmSAcInABNEx6y7IPwyNcG+H01apKts9hX
      SZJyYMU6Z8Aq0BcIPN/yx9u4kSX/NFYZsGg6X4nqKMa6PkZsNM37j+3DEdJkohoKI83JfBp3kDB2
      sKZR7cGceBrI7W32kkkKDTo0vMNWLaXidTfNuQY+OAeOL+ZG1ZuJdMQwZSrwR2BWJOjN1ZDzJ8ew
      MH/Ek8kpW9iBuERSc2yYEwSW+bHWTKx6AtSZxvCiptVPWOcqSE0YBNZ3WX1Cdv8j3OCVzWeqEnHw
      RmiKvwIDAQAB
      """;
}
