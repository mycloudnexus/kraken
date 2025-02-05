package com.consoleconnect.kraken.operator.gateway.template;

import com.consoleconnect.kraken.operator.core.toolkit.JsonToolkit;
import com.consoleconnect.kraken.operator.core.toolkit.StringUtils;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.expression.MapAccessor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.expression.*;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.expression.spel.support.StandardTypeConverter;
import org.springframework.integration.json.JsonPropertyAccessor;

@Slf4j
public class SpELEngine implements BeanFactoryAware {

  private static final ExpressionParser expressionParser = new SpelExpressionParser();
  private static BeanFactory beanFactory;
  private static final String MASK_STAR = "[*]";

  private final StandardEvaluationContext evaluationContext;
  private final ParserContext parserContext =
      new ParserContext() {
        @Override
        public boolean isTemplate() {
          return true;
        }

        @Override
        public String getExpressionPrefix() {
          return "${";
        }

        @Override
        public String getExpressionSuffix() {
          return "}";
        }
      };

  public SpELEngine(Map<String, Object> variables) {
    evaluationContext = new StandardEvaluationContext(variables);
    evaluationContext.addPropertyAccessor(new JsonPropertyAccessor());
    evaluationContext.addPropertyAccessor(new MapAccessor());

    GenericConversionService conversionService = new DefaultConversionService();
    conversionService.addConverter(new ConvertLinkedHashMapToString());
    evaluationContext.setTypeConverter(new StandardTypeConverter(conversionService));
  }

  public boolean isTrue(String expression) {
    return Boolean.TRUE.equals(evaluate(expression, Boolean.class));
  }

  public Object evaluate(String expression) {
    return evaluate(expression, Object.class);
  }

  public <T> T evaluate(String expression, Class<T> clazz) {
    try {
      return expressionParser
          .parseExpression(expression, parserContext)
          .getValue(evaluationContext, clazz);
    } catch (Exception ex) {
      return null;
    }
  }

  public <T> T evaluateWithoutSuppressException(String expression, Class<T> clazz) {
    return expressionParser
        .parseExpression(expression, parserContext)
        .getValue(evaluationContext, clazz);
  }

  public static <T> T evaluateWithoutSuppressException(
      String expression, Map<String, Object> variables, Class<T> tClass) {
    SpELEngine spELEngine = new SpELEngine(variables);
    return spELEngine.evaluateWithoutSuppressException(expression, tClass);
  }

  public static boolean isTrue(String expression, Map<String, Object> variables) {
    return new SpELEngine(variables).isTrue(expression);
  }

  public static <T> T evaluate(String expression, Map<String, Object> variables, Class<T> clazz) {
    SpELEngine spELEngine = new SpELEngine(variables);
    if (beanFactory != null) {
      spELEngine.evaluationContext.setBeanResolver(new BeanFactoryResolver(SpELEngine.beanFactory));
    }
    return spELEngine.evaluate(expression, clazz);
  }

  @Override
  public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
    setBeanFactoryStatic(beanFactory);
    evaluationContext.setBeanResolver(new BeanFactoryResolver(SpELEngine.beanFactory));
  }

  private static void setBeanFactoryStatic(BeanFactory beanFactory) {
    SpELEngine.beanFactory = beanFactory;
  }

  public static class ConvertLinkedHashMapToString
      implements Converter<LinkedHashMap<?, ?>, String> {
    @Override
    public String convert(LinkedHashMap<?, ?> source) {
      return escape(JsonToolkit.toJson(source));
    }
  }

  public static String evaluate(
      Object expression, Map<String, Object> context, boolean postRequest) {
    Object o = evaluateObject(expression, context, postRequest);
    return o instanceof String str ? str : JsonToolkit.toJson(o);
  }

  public static String evaluate(Object expression, Map<String, Object> context) {
    Object o = evaluateObject(expression, context, false);
    return o instanceof String str ? str : JsonToolkit.toJson(o);
  }

  public static Object evaluateObject(
      Object expression, Map<String, Object> context, boolean postRequest) {
    try {
      if (expression instanceof String str) {
        return parseStr(str, context, postRequest);
      } else if (expression instanceof Map<?, ?> map) {
        return evaluateMap((Map<String, Object>) map, context, postRequest);
      } else if (expression instanceof List<?> list) {
        return list.stream().map(item -> evaluateObject(item, context, postRequest)).toList();
      } else {
        return expression;
      }
    } catch (Exception e) {
      log.warn("spel parse object error: {}", e.getMessage());
    }
    return evaluate(String.valueOf(expression), context, String.class);
  }

  private static Object parseStr(String str, Map<String, Object> context, Boolean postRequest) {
    try {
      Map<String, Object> map = JsonToolkit.fromJson(StringUtils.compact(str), Map.class);
      return evaluateMap(map, context, postRequest);
    } catch (Exception e) {
      List<Map<String, Object>> list = JsonToolkit.fromJson(StringUtils.compact(str), List.class);
      return evaluateObject(list, context, postRequest);
    }
  }

  public static void parseToList(
      Object expression, Map<String, Object> context, List<Map<String, Object>> iterator) {
    if (expression instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> entry : ((Map<String, Object>) map).entrySet()) {
        if (entry.getValue() instanceof List<?> tmp) {
          Map<String, Object> maskMap = (Map<String, Object>) tmp.get(0);
          if (!JsonToolkit.toJson(maskMap).contains(MASK_STAR)) {
            return;
          }
          parseToList(maskMap, context, (List<Map<String, Object>>) tmp);
        } else if (entry.getValue() instanceof String str && str.contains(MASK_STAR)) {
          handleStr(str, iterator, context);
          break;
        } else {
          parseToList(entry.getValue(), context, iterator);
        }
      }
    }
  }

  private static void handleStr(
      String str, List<Map<String, Object>> iterator, Map<String, Object> context) {
    if (str.contains(MASK_STAR)) {
      String prefix = str.substring(0, str.indexOf(MASK_STAR));
      List<String> list = SpELEngine.evaluate(String.format("%s}", prefix), context, List.class);
      Map<String, Object> iteratorMap = iterator.get(0);
      iterator.remove(0);
      if (CollectionUtils.isEmpty(list)) {
        return;
      }
      for (int i = 0; i < list.size(); i++) {
        iterator.add(
            JsonToolkit.fromJson(
                JsonToolkit.toJson(iteratorMap).replace(MASK_STAR, String.format("[%d]", i)),
                Map.class));
      }
    }
  }

  public static Object evaluateMap(
      Map<String, Object> map, Map<String, Object> context, boolean postRequest) {
    if (map == null) {
      return null;
    }
    for (Map.Entry<String, Object> v : map.entrySet()) {
      try {
        if (v.getValue() != null && v.getValue() instanceof String value) {
          Object evaluate = SpELEngine.evaluate(value, context, Object.class);
          v.setValue(evaluate == null && !postRequest ? value : evaluate);
        } else {
          v.setValue(evaluateObject(v.getValue(), context, postRequest));
        }
      } catch (Exception e) {
        log.warn("parse map error!");
      }
    }
    return map;
  }

  public static String escape(String raw) {
    String escaped = raw;
    escaped = escaped.replace("\\", "\\\\");
    escaped = escaped.replace("\"", "\\\"");
    escaped = escaped.replace("\b", "\\b");
    escaped = escaped.replace("\f", "\\f");
    escaped = escaped.replace("\n", "\\n");
    escaped = escaped.replace("\r", "\\r");
    escaped = escaped.replace("\t", "\\t");
    return escaped;
  }
}
