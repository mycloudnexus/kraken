package com.consoleconnect.kraken.operator.core.service;

import static com.consoleconnect.kraken.operator.core.toolkit.Constants.DOT;
import static com.consoleconnect.kraken.operator.core.toolkit.Constants.SELLER_CONTACT_SUFFIX;

import com.consoleconnect.kraken.operator.core.enums.ParentProductTypeEnum;
import org.apache.commons.lang3.StringUtils;

public interface AssetKeyGenerator {

  default String generateSellerContactKey(String componentKey, String parentProductType) {
    if (StringUtils.isBlank(parentProductType)) {
      return componentKey
          + DOT
          + ParentProductTypeEnum.ACCESS_ELINE.getKind()
          + DOT
          + SELLER_CONTACT_SUFFIX;
    }
    return componentKey + DOT + parentProductType + DOT + SELLER_CONTACT_SUFFIX;
  }
}
