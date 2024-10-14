package com.consoleconnect.kraken.operator.core;

import com.consoleconnect.kraken.operator.core.toolkit.Paging;
import com.consoleconnect.kraken.operator.core.toolkit.PagingHelper;
import java.util.Arrays;
import java.util.List;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

class PageHelperTest {
  @Test
  void testToPageNoSubList() {
    List<Integer> result = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
    PagingHelper.toPageNoSubList(null, null, null, 1l);
    Paging<Integer> pageNoSubList = PagingHelper.toPageNoSubList(result, 0, 10, 1l);
    MatcherAssert.assertThat(pageNoSubList.getData(), Matchers.hasSize(7));
  }
}
