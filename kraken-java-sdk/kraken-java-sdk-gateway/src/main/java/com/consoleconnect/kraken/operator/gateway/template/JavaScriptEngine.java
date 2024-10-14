package com.consoleconnect.kraken.operator.gateway.template;

import com.consoleconnect.kraken.operator.core.exception.KrakenException;
import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import java.io.IOException;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.Engine;
import org.graalvm.polyglot.Source;
import org.graalvm.polyglot.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class JavaScriptEngine {
  public static final String JS = "js";

  public static final String CONTEXT_INPUT = "input";

  private final Engine engine;
  private final Map<String, Source> sources = new HashMap<>();

  public JavaScriptEngine() {
    engine = Engine.create(JS);
  }

  private Optional<Source> getSourceCode(String scriptId) {
    if (sources.containsKey(scriptId)) {
      return Optional.of(sources.get(scriptId));
    }
    return Optional.empty();
  }

  public void addSourceIfNotPresent(String scriptId, String script) {
    if (!sources.containsKey(scriptId)) {
      addSource(scriptId, script);
    }
  }

  public void addSource(String scriptId, String script) {
    try {
      Source source =
          Source.newBuilder(JS, script, scriptId)
              .cached(true)
              //              .mimeType("application/javascript+module")
              .build();
      sources.put(scriptId, source);
    } catch (IOException ex) {
      log.error("Failed to add source code", ex);
      throw KrakenException.internalError("Failed to add source code");
    }
  }

  public String execute(String scriptId, Object input) {
    Source source =
        getSourceCode(scriptId)
            .orElseThrow(() -> KrakenException.badRequest("Script not found: " + scriptId));

    String inputJson = JsonToolkit.toJson(input);

    log.info("Executing script: {}", scriptId);
    try (Context context = Context.newBuilder().engine(engine).build()) {
      context.getBindings(JS).putMember(CONTEXT_INPUT, inputJson);
      Value ret = context.eval(source);

      String result = null;
      if (ret.canExecute()) {
        result = ret.execute(inputJson).asString();
      } else {
        result = ret.asString();
      }
      log.info("Executed script: {},result:{}", scriptId, result);
      return result;
    } catch (Exception e) {
      log.error("Failed to execute script", e);
      throw KrakenException.internalError("Failed to execute script: " + scriptId);
    }
  }
}
