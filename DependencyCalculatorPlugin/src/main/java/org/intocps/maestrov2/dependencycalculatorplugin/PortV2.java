package org.intocps.maestrov2.dependencycalculatorplugin;

import org.apache.commons.lang3.builder.HashCodeBuilder;

public class PortV2 {
    final String instanceName;
    final String fmuName;
    final String variableName;

    public PortV2(String instanceName, String fmuName, String variableName) {
        this.instanceName = instanceName;
        this.fmuName = fmuName;
        this.variableName = variableName;
    }

    // JGRAPH uses equals and hashCode to detect whether two instances are to be considered the same.
    // https://github.com/jgrapht/jgrapht/wiki/EqualsAndHashCode
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PortV2) {
            PortV2 objP = (PortV2) obj;
            return this.instanceName.equals(objP.instanceName) &&
                    this.fmuName.equals(objP.fmuName) &&
                    this.variableName.equals(objP.variableName);
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        HashCodeBuilder builder = new HashCodeBuilder();
        builder.append(instanceName);
        builder.append(fmuName);
        builder.append(variableName);
        return builder.hashCode();
    }

    @Override
    public String toString() {
        return String.format("%s.%s.%s", fmuName, instanceName, variableName);
    }
}
