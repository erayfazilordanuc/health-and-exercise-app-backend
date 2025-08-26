package exercise.Exercise.mappers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class LongListConverter implements AttributeConverter<List<Long>, String> {
  @Override
  public String convertToDatabaseColumn(List<Long> list) {
    return (list == null || list.isEmpty()) ? null
        : list.stream().map(String::valueOf).collect(Collectors.joining(","));
  }

  @Override
  public List<Long> convertToEntityAttribute(String joined) {
    return (joined == null || joined.isBlank()) ? new ArrayList<>()
        : Arrays.stream(joined.split(",")).map(Long::valueOf).toList();
  }
}