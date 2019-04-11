package org.intocps.maestrov2.dependencycalculatorplugin;

public class CycleCheckResult {
    public final boolean cyclic;
    public final String cycles;

    public CycleCheckResult(boolean cyclic, String cycles) {
        this.cyclic = cyclic;
        if(this.cyclic)
        {
            this.cycles = cycles;
        }
        else
        {
            this.cycles = null;
        }
    }


}
