package org.gaussian.vgraph.domain;

import java.util.List;

public record UpdateSchemaDirectiveRequest(String name, List<UpdateSchemaArgumentRequest> arguments) {

}
