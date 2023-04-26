package org.gaussian.vgraph.domain;

import java.util.List;

public record VariableDirective(String name, List<VariableDirectiveArgument> arguments) {
}
