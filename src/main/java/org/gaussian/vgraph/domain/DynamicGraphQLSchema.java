package org.gaussian.vgraph.domain;

import graphql.schema.*;
import org.apache.commons.lang3.StringUtils;

import java.util.*;

import static java.util.stream.Collectors.toList;

public class DynamicGraphQLSchema {

    private static final String INDENT = "    ";
    private static final String COMMENT_BLOCK_BEGIN = "\"\"\"";
    private static final String COMMENT_BLOCK_END = "\"\"\"";

    public final List<GraphQLFieldDefinition> fieldDefinitions;
    public Map<String, Namespace> namespaces;

    public DynamicGraphQLSchema(List<GraphQLFieldDefinition> fieldDefinitions) {
        this.fieldDefinitions = fieldDefinitions;
        this.namespaces = getNamespaces(fieldDefinitions);
    }

    public String update(UpdateSchemaRequest request) {
        String namespaceId = request.namespace();
        String variableName = request.variable();
        String variableType = request.type();

        Namespace namespace = namespaces.get(namespaceId);
        Map<String, Variable> variables = namespace.variables;
        Variable variable = variables.getOrDefault(variableName, createVariable(variableName, variableType, request.directives()));
        variables.put(variable.name, variable);
        String updatedSchema = toString();
        return updatedSchema;
    }

    private Variable createVariable(String name, String type, List<VariableDirective> directives) {
        return new Variable(name, type, "tbd", directives);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        buffer.append("directive @countries(");
        buffer.append("    codes: [String] = []");
        buffer.append(") on FIELD_DEFINITION");
        buffer.append("\n\n");

        for (Map.Entry<String, Namespace> namespaceEntry : namespaces.entrySet()) {
            Namespace namespace = namespaceEntry.getValue();
            appendDescription(buffer, namespace.description, 0);
            buffer.append("type " + namespace.typeName + " {");
            buffer.append("\n");

            for (Map.Entry<String, Variable> variableEntry : namespace.variables.entrySet()) {
                Variable variable = variableEntry.getValue();
                appendDescription(buffer, variable.description, 1);
                buffer.append(INDENT + variable.name + ": " + variable.type + " ");

                for (VariableDirective directive : variable.directives) {
                    buffer.append(String.format("@%s(", directive.name()));
                    for (VariableDirectiveArgument directiveArgument : directive.arguments()) {
                        buffer.append(String.format("%s: ", directiveArgument.name()));
                        List<String> normalizedValues = directiveArgument.values().stream().map(value -> String.format("\"%s\"", value)).collect(toList());
                        buffer.append(String.format("[%s]", String.join(",", normalizedValues)));
                    }
                }
                if (variable.directives.size() > 0) {
                    buffer.append(")");
                }
                buffer.append("\n");
            }
            buffer.append("}\n\n");
        }

        buffer.append("type Query {\n");
        for (Map.Entry<String, Namespace> namespaceEntry : namespaces.entrySet()) {
            Namespace namespace = namespaceEntry.getValue();
            appendDescription(buffer, namespace.description, 1);
            buffer.append(String.format("%s%s(", INDENT, namespace.fieldName));
            List<String> normalizedArguments = namespace.arguments.stream().map(argument -> String.format("%s: %s", argument.name, argument.type)).collect(toList());
            buffer.append(String.join(",", normalizedArguments));
            buffer.append(String.format("): %s\n", namespace.typeName));
        }

        buffer.append("}\n");
        buffer.append(String.format("schema {\n%squery: Query\n}", INDENT));

        return buffer.toString();
    }

    private static void appendDescription(StringBuffer buffer, String description, int numIndentations) {
        final String INDENTATION = StringUtils.repeat(INDENT, numIndentations);
        if (description != null && !description.trim().isEmpty()) {
            String sanitizedDescription = description.trim().replace("\"", "\'");
            if (sanitizedDescription.split("\\R").length > 1) {
                buffer.append(INDENTATION + COMMENT_BLOCK_BEGIN);
                buffer.append("\n");
                Arrays.stream(sanitizedDescription.split("\\R")).forEach(str -> {
                    buffer.append(INDENTATION + str);
                    buffer.append("\n");
                });
                buffer.append(INDENTATION + COMMENT_BLOCK_END);
                buffer.append("\n");
            } else {
                buffer.append(INDENTATION + "\"" + sanitizedDescription + "\"");
                buffer.append("\n");
            }
        }
    }

    public Map<String, Namespace> getNamespaces(List<GraphQLFieldDefinition> fieldDefinitions) {
        Map<String, Namespace> namespaces = new HashMap<>();

        for (GraphQLFieldDefinition fieldDefinition : fieldDefinitions) {
            Map<String, Variable> variables = new HashMap<>();
            List<NamespaceArgument> namespaceArguments = new ArrayList<>();
            GraphQLObjectType graphQLObjectType = (GraphQLObjectType) fieldDefinition.getType();
            String typeName = graphQLObjectType.getName();
            String fieldName = fieldDefinition.getName();
            String description = fieldDefinition.getDescription();
            List<GraphQLFieldDefinition> fieldTypeDefinitions = graphQLObjectType.getFieldDefinitions();

            for (GraphQLArgument fieldDefinitionArgument : fieldDefinition.getArguments()) {
                String fieldDefinitionArgumentType = ((GraphQLScalarType) fieldDefinitionArgument.getType()).getName();
                String fieldDefinitionArgumentName = fieldDefinitionArgument.getName();
                NamespaceArgument argument = new NamespaceArgument(fieldDefinitionArgumentName, fieldDefinitionArgumentType);
                namespaceArguments.add(argument);
            }

            for (GraphQLFieldDefinition fieldTypeDefinition : fieldTypeDefinitions) {
                String variableDescription = fieldTypeDefinition.getDescription();
                String variableName = fieldTypeDefinition.getName();
                String variableType = ((GraphQLScalarType) fieldTypeDefinition.getType()).getName();
                List<VariableDirective> directives = new ArrayList<>();

                for (GraphQLAppliedDirective directive : fieldTypeDefinition.getAppliedDirectives()) {
                    //Map<String, List<String>> directiveValues = new HashMap<>();
                    List<VariableDirectiveArgument> directiveArguments = new ArrayList<>();
                    for (GraphQLAppliedDirectiveArgument directiveArgument : directive.getArguments()) {
                        if (directiveArgument.getType() instanceof GraphQLList) {
                            List<String> directiveArgumentValues = directiveArgument.getValue();
                            directiveArguments.add(new VariableDirectiveArgument(directiveArgument.getName(), directiveArgumentValues));
                        }
                    }
                    VariableDirective newDirective = new VariableDirective(directive.getName(), directiveArguments);
                    directives.add(newDirective);
                }

                Variable variable = new Variable(variableName, variableType, variableDescription, directives);
                variables.put(variable.name, variable);
            }

            Namespace namespace = new Namespace(fieldName, typeName, description, variables, namespaceArguments);
            namespaces.put(namespace.fieldName, namespace);
        }
        return namespaces;
    }

}
