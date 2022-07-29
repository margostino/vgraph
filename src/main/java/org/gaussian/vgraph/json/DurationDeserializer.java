package org.gaussian.vgraph.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.time.Duration;

import static java.time.Duration.ofMillis;

public class DurationDeserializer extends JsonDeserializer<Duration> {

  @Override
  public Duration deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
    final JsonNode node = jp.getCodec().readTree(jp);
    final String fullDuration = node.textValue();
    final long durationInMs = Long.valueOf(fullDuration.split(" ")[0]); // TODO: validate
    return ofMillis(durationInMs);
  }
}
