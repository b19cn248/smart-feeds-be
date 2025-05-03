package com.olh.feeds.core.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MapperUtils {
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static ObjectMapper getMapper() {
    MAPPER.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
    return MAPPER;
  }

  public static <T> Map<String, T> converToMap(T object) {
    return getMapper().convertValue(object, Map.class);
  }

  public static <T> Set<T> convertListToSet(List<T> list) {
    return new HashSet<>(list);
  }

  public static <T> List<T> convertRawDataToList(Object data) {
    return data == null ? null : MAPPER.convertValue(data, new TypeReference<>() {
    });
  }
}
