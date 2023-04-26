package org.gaussian.vgraph.domain;

import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class Variable {
    public String name;
    public String type;
    public String description;
    public List<VariableDirective> directives;
}
