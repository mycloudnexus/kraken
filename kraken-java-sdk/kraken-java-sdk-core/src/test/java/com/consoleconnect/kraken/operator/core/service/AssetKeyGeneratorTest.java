package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.SELLER_CONTACT_SUFFIX;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

class AssetKeyGeneratorTest implements AssetKeyGenerator {

  @ParameterizedTest
  @MethodSource(value = "buildSellerContactKeys")
  void givenBlankParentProductType_whenCreate_thenReturnOK(Pair<String, String> pair) {
    String key = generateSellerContactKey(pair.getLeft(), pair.getRight());
    Assertions.assertNotNull(key);
    Assertions.assertTrue(key.endsWith(SELLER_CONTACT_SUFFIX));
  }

  public static List<Pair<String, String>> buildSellerContactKeys() {
    List<Pair<String, String>> list = new ArrayList<>();
    Pair<String, String> pair1 = Pair.of("mef.sonata.api.order", "access.eline");
    Pair<String, String> pair2 = Pair.of("mef.sonata.api.quote", "access.eline");
    Pair<String, String> pair3 = Pair.of("mef.sonata.api.quote", "");
    list.add(pair1);
    list.add(pair2);
    list.add(pair3);
    return list;
  }
}
