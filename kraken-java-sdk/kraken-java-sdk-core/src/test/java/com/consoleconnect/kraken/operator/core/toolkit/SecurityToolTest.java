package com.consoleconnect.kraken.operator.core.toolkit;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@Slf4j
class SecurityToolTest {

    @Test
    void testNegativeExpression() {
        String s = "@{{buyerId==null?0:T(java.lang.Runtime).getRuntime().exec(new java.lang.String[]{body.p[0].body.p[1].body.p[2]})}}";
        Assertions.assertThrows(KrakenException.class, () ->  SecurityTool.evaluate(s));
    }
}
