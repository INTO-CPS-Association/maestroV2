package org.intocps.maestrov2.dependencycalculatorplugin;

public class IOResult {
    public final CycleCheckResult cycleCheckResult;
    public final scala.collection.immutable.List<PortV2> ordered;

    public IOResult(CycleCheckResult result) {
        this(result, null);

    }

    public IOResult(CycleCheckResult result, scala.collection.immutable.List<PortV2> ordered) {
        if (result != null) {
            this.cycleCheckResult = result;
            if (result.cyclic) {
                if (ordered != null) {
                    throw new IllegalArgumentException("There are cycles, but an order has been supplied.");
                }
                else {
                    this.ordered = null;
                }
            } else {
                if (ordered == null)
                    throw new IllegalArgumentException("There are no cycles, but an order has NOT been supplied.");
                else
                    this.ordered = ordered;
            }
        } else {
            throw new IllegalArgumentException("Cycle check result has not been supplied.");
        }
    }


}
