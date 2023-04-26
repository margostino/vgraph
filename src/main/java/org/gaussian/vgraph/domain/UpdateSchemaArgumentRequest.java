package org.gaussian.vgraph.domain;

import java.util.List;

public record UpdateSchemaArgumentRequest(String name, List<String> values) {

}
