import org.maestro.ast.expression.*;
import org.maestro.ast.specification.ASimulationSpecification;
import org.maestro.ast.statements.AAssignmentStm;
import org.maestro.ast.statements.PStm;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    static Factory f = new Factory();

    public static void main() {


        ASimulationSpecification spec = new ASimulationSpecification();

        final String tankDecl = "tankDecl";
        final String ctrlDecl = "ctrlDecl";

        spec.getBody().add(f.assignment(ctrlDecl, new ALoadExp(f.newStringLiteral("controller.fmu"), f.newIdentifierExp("fmi2"))));
        spec.getBody().add(f.assignment(tankDecl, new ALoadExp(f.newStringLiteral("tank.fmu"), f.newIdentifierExp("fmi2"))));

        //TODO how do I know that I need to make these instances?
        final String tank = "tank";
        final String ctrl = "ctrl";

        spec.getBody().add(f.assignment(ctrl,
                f.newApply(ctrlDecl, "instantiate", f.newStringLiteral("controller"), f.newBoolLiteral(false), f.newBoolLiteral(true))));
        spec.getBody().add(f.assignment(tank,
                f.newApply(ctrlDecl, "instantiate", f.newStringLiteral("tank"), f.newBoolLiteral(false), f.newBoolLiteral(true))));


        spec.getBody().add(f.assignment(ctrl,
                f.newApply(ctrlDecl, "setupExperiment", f.newVariableExp(ctrl), f.newBoolLiteral(false), f.newRealLiteral(0.0), f.newRealLiteral(0.0),
                        f.newBoolLiteral(true), f.newRealLiteral(0.0))));
        spec.getBody().add(f.assignment(ctrl,
                f.newApply(tankDecl, "setupExperiment", f.newVariableExp(tank), f.newBoolLiteral(false), f.newRealLiteral(0.0), f.newRealLiteral(0.0),
                        f.newBoolLiteral(true), f.newRealLiteral(0.0))));


        spec.getBody().add(setReal("s", ctrlDecl, ctrl, ints(1, 2), reals(1, 2)));

        spec.getBody().add(f.assignment(ctrl, f.newApply(ctrlDecl, "enterInitializationMode", f.newVariableExp(ctrl))));
        spec.getBody().add(f.assignment(ctrl, f.newApply(tankDecl, "enterInitializationMode", f.newVariableExp(tank))));

        final String level = "level";
        final String valve = "valve";

    }

    public static int[] ints(int... v) {
        return v;
    }

    public static double[] reals(double... v) {
        return v;
    }

    public static PStm setReal(String result, String decl, String comp, int[] vref, double[] values) {
        return f.assignment(result, f.newApply(decl, "setReal", f.newVariableExp(comp),
                f.newSeqComp(Arrays.stream(vref).mapToObj(v -> f.newIntLiteral(v)).collect(Collectors.toList())),
                f.newSeqComp(Arrays.stream(values).mapToObj(v -> f.newRealLiteral(v)).collect(Collectors.toList()))));
    }

    @SuppressWarnings("all")
    public static class Factory {

        public AIdentifierExp newIdentifierExp(String id) {
            return new AIdentifierExp(id);
        }

        public AStringLiteralExp newStringLiteral(String value) {
            return new AStringLiteralExp(value);
        }

        public PStm assignment(String target, PExp source) {
            AAssignmentStm assignmentStm = new AAssignmentStm();
            assignmentStm.setExp(source);
            assignmentStm.setTarget(newIdentifierExp(target));
            return assignmentStm;
        }

        public PExp newApply(String rootId, String functionId, PExp... args) {
            AApplyExp apply = new AApplyExp();
            apply.setRoot(newIdentifierExp(rootId));
            apply.setFunctionName(newIdentifierExp(functionId));
            apply.setArgs(Arrays.asList(args));
            return apply;
        }

        public PExp newBoolLiteral(boolean b) {
            return new ABooleanLiteralExp(b);
        }

        public PExp newVariableExp(String ctrl) {
            AVariableExp v = new AVariableExp();
            v.setName(newIdentifierExp(ctrl));
            return v;
        }

        public PExp newRealLiteral(double v) {
            ARealLiteralExp l = new ARealLiteralExp();
            l.setValue(v);
            return l;
        }

        public PExp newSeqComp(List<? extends PExp> members) {
            ASeqCompExp comp = new ASeqCompExp();
            comp.setMembers(members);
            return comp;
        }

        public PExp newIntLiteral(int v) {
            return new AIntLiteralExp(v);

        }
    }

}
