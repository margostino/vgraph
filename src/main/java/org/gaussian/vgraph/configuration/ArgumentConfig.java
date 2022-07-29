package org.gaussian.vgraph.configuration;


import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

public class ArgumentConfig {

    public String name;
    public boolean isRequired = false;

    public static ArgumentConfig newArgumentConfig(String name, boolean isRequired) {
        ArgumentConfig argumentConfig = new ArgumentConfig();
        argumentConfig.name = name;
        argumentConfig.isRequired = isRequired;
        return argumentConfig;
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SIMPLE_STYLE);
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        } else if (other instanceof ArgumentConfig){
            ArgumentConfig otherArgumentConfig = (ArgumentConfig) other;
            return this.name.equals(otherArgumentConfig.name) && this.isRequired == otherArgumentConfig.isRequired;
        } else {
            return false;
        }

    }
}
