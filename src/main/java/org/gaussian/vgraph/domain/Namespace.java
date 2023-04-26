package org.gaussian.vgraph.domain;

import java.util.List;
import java.util.Map;

public class Namespace {
    public String fieldName;
    public String typeName;
    public String description;
    public Map<String, Variable> variables;
    public List<NamespaceArgument> arguments;

    public Namespace(String fieldName, String typeName, String description, Map<String, Variable> variables, List<NamespaceArgument> arguments) {
        this.fieldName = fieldName;
        this.typeName = typeName;
        this.description = description;
        this.variables = variables;
        this.arguments = arguments;
    }
}
