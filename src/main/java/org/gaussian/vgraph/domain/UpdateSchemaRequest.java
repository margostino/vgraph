package org.gaussian.vgraph.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record UpdateSchemaRequest(String namespace, @JsonProperty("variable_name") String variable, String type, String description, List<VariableDirective> directives) {

}
