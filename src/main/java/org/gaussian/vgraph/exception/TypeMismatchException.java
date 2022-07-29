package org.gaussian.vgraph.exception;

public class TypeMismatchException extends RuntimeException {
    private final String field;
    private final String sourceType;
    private final String graphQLType;

    public TypeMismatchException(String field, String sourceType, String graphQLType) {
        this.field = field;
        this.sourceType = sourceType;
        this.graphQLType = graphQLType;
    }

    public String getField() {
        return field;
    }

    public String getSourceType() {
        return sourceType;
    }

    public String getGraphQLType() {
        return graphQLType;
    }
}
