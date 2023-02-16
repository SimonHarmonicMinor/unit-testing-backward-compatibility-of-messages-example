package com.company;

import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.stream.Stream;

import lombok.SneakyThrows;

import static java.util.Comparator.comparing;
import static java.util.Objects.requireNonNullElse;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class BackwardCompatibilityTest {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideMessagesExamples")
    void shouldRemainBackwardCompatibility(String filename, String json) {
        final var orderCreated = assertDoesNotThrow(
            () -> objectMapper.readValue(json, OrderCreated.class),
            "Couldn't parse OrderCreated from JSON for filename: " + filename
        );
        final var serializedJson = assertDoesNotThrow(
            () -> objectMapper.writeValueAsString(orderCreated),
            "Couldn't serialize OrderCreated to JSON from filename: " + filename
        );
        final var expected = new JSONObject(json);
        final var actual = new JSONObject(serializedJson);
        JSONAssert.assertEquals(
            expected,
            actual,
            false
        );
    }

    @SneakyThrows
    private static Stream<Arguments> provideMessagesExamples() {
        final var resourceFolder = Thread.currentThread().getContextClassLoader().getResources("backward-compatibility").nextElement();
        final var fileInfos = Files.walk(Path.of(resourceFolder.toURI()))
                                  .filter(path -> path.toFile().isFile())
                                  .sorted(comparing(path -> path.getFileName().toString()))
                                  .map(file -> {
                                          try {
                                              return new FileInfo(
                                                  file.getFileName().toString(),
                                                  Files.readString(file));
                                          } catch (IOException e) {
                                              throw new RuntimeException(e);
                                          }
                                      }
                                  )
                                  .toList();
        final var argumentsList = new ArrayList<Arguments>();
        for (int i = 0; i < fileInfos.size(); i++) {
            JSONObject initialJson = null;
            for (int j = 0; j <= i; j++) {
                if (j == 0) {
                    initialJson = new JSONObject(fileInfos.get(i).content);
                }
                final var curr = fileInfos.get(j);
                deepMerge(new JSONObject(curr.content), initialJson);
                if (j == i) {
                    argumentsList.add(Arguments.arguments(curr.name, initialJson.toString()));
                }
            }
        }
        return argumentsList.stream();
    }

    private record FileInfo(String name, String content) {
    }

    @SneakyThrows
    private static void deepMerge(JSONObject source, JSONObject target) {
        final var names = requireNonNullElse(source.names(), new JSONArray());
        for (int i = 0; i < names.length(); i++) {
            final String key = (String) names.get(i);
            Object value = source.get(key);
            if (!target.has(key)) {
                // new value for "key":
                target.put(key, value);
            } else {
                // existing value for "key" - recursively deep merge:
                if (value instanceof JSONObject valueJson) {
                    deepMerge(valueJson, target.getJSONObject(key));
                } else {
                    target.put(key, value);
                }
            }
        }
    }
}