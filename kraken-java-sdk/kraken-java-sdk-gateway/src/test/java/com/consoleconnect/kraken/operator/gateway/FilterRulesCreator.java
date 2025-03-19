package com.consoleconnect.kraken.operator.gateway;

import com.consoleconnect.kraken.operator.core.model.FilterRule;
import java.util.ArrayList;
import java.util.List;

public interface FilterRulesCreator {

  default List<FilterRule> buildValidFilterRules() {
    List<FilterRule> filterRules = new ArrayList<>();
    FilterRule filterRule = new FilterRule();
    filterRule.setQueryPath("$.body.quoteItem[0].product.id");
    filterRule.setFilterKey("action");
    filterRule.setFilterVal("add");
    filterRule.setFilterPath("$.productOrderItem[?]");
    filterRules.add(filterRule);
    return filterRules;
  }
}
