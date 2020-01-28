package org.intocps.maestro;

import org.maestro.ast.analysis.AnalysisException;
import org.maestro.ast.expression.*;
import org.maestro.ast.specification.ASimulationSpecification;
import org.maestro.ast.statements.*;
import org.maestro.ast.types.AArrayType;
import org.maestro.ast.types.ARealBasicType;
import org.maestro.ast.types.*;
import org.maestro.ast.types.SBasicType;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    static Factory f = new Factory();

    public static void main(String[] args) throws AnalysisException {


        ABlockStm list = f.newBlock();

        list.getBody().add(f.newImport("fmi2"));

        final String tankDecl = "tankDecl";
        final String ctrlDecl = "ctrlDecl";

        list.getBody().add(f.newVariableDecl(tankDecl, f.newNamedType("fmi2"), null));
        list.getBody().add(f.newVariableDecl(ctrlDecl, f.newNamedType("fmi2"), null));

        list.getBody().add(new ALoadStm(f.newStringLiteral(new File("src/main/resources/watertankcontroller-c/binaries/darwin64" +
                "/watertankcontroller-c.dylib").getAbsolutePath()), f.newIdentifierExp("fmi2"),
                new ATupleIdentifierExp(Arrays.asList(f.newIdentifierExp("errorCode"), f.newIdentifierExp(ctrlDecl)))));

        list.getBody().add(new ALoadStm(f.newStringLiteral(new File("src/main" +
                "/resources" +
                "/singlewatertank-20sim/binaries/darwin64/singlewatertank-20sim.dylib").getAbsolutePath()), f.newIdentifierExp("fmi2"),
                new ATupleIdentifierExp(Arrays.asList(f.newIdentifierExp("errorCode1"), f.newIdentifierExp(tankDecl)))));

        //        list.getBody().add(f.assignment("err", new ALoadExp(f.newStringLiteral("controller.fmu"), f.newIdentifierExp("fmi2"))));
        //        list.getBody().add(f.assignment("err", new ALoadExp(f.newStringLiteral("tank.fmu"), f.newIdentifierExp("fmi2"))));

        //TODO how do I know that I need to make these instances?
        final String tank = "tank";
        final String ctrl = "ctrl";

/*fmi2Component fmi2Instantiate(fmi2String instanceName,
                                         fmi2Type fmuType, fmi2String fmuGUID,
                                         fmi2String fmuResourceLocation,
                                         const fmi2CallbackFunctions *functions,
                                         fmi2Boolean visible,
                                         fmi2Boolean loggingOn)*/
        list.getBody().add(f.newVariableDecl(ctrl, f.newNamedType("auto"),
                f.newApply(ctrlDecl, "instantiate", f.newStringLiteral("controller"), f.newStringLiteral("{8c4e810f-3df3-4a00-8276-176fa3c9f000}"), f.newStringLiteral("uri"),
                        f.newBoolLiteral(false), f.newBoolLiteral(true))));


        list.getBody().add(f.newVariableDecl(tank, f.newNamedType("auto"),
                f.newApply(tankDecl, "instantiate", f.newStringLiteral("tank"), f.newStringLiteral("{cfc65592-9ece-4563-9705-1581b6e7071c}"),
                        f.newStringLiteral("uri"),
                        f.newBoolLiteral(false), f.newBoolLiteral(true))));

        // TODO: Missing to set parameters from tank: 0,1,2,3,4,5,6

        list.getBody().add(f.newVariableDecl("s", f.newNamedType("fmi2Status"), null));

        //fmi2Status fmi2SetupExperiment(
        //        fmi2Component c, fmi2Boolean toleranceDefined, fmi2Real tolerance,
        //        fmi2Real startTime, fmi2Boolean stopTimeDefined, fmi2Real stopTime)
        list.getBody().add(f.assignment("s",
                f.newApply(ctrlDecl, "setupExperiment", f.newVariableExp(ctrl), f.newBoolLiteral(false), f.newRealLiteral(0.0), f.newRealLiteral(0.0),
                        f.newBoolLiteral(true), f.newRealLiteral(10.0))));
        list.getBody().add(f.assignment("s",
                f.newApply(tankDecl, "setupExperiment", f.newVariableExp(tank), f.newBoolLiteral(false), f.newRealLiteral(0.0), f.newRealLiteral(0.0),
                        f.newBoolLiteral(true), f.newRealLiteral(10.0))));


//        list.getBody().add(setReal("s", ctrlDecl, ctrl, ints(0, 1), reals(1, 2)));


        list.getBody().add(f.assignment("s", f.newApply(ctrlDecl, "enterInitializationMode", f.newVariableExp(ctrl))));
        list.getBody().add(f.assignment("s", f.newApply(tankDecl, "enterInitializationMode", f.newVariableExp(tank))));

        /*Set[
			SetCMD(tank-(t)-(0,5,1,6,2,3,4)),
			SetCMD(control-(c)-(0,1))],
		Seq[
			GetCMD(control-(c)-(4)),
			SetCMD(tank-(t)-(16)),
			GetCMD(tank-(t)-(17)),
			SetCMD(control-(c)-(3))]],
	Set[*/


        final String level = "level";
        final String valve = "valve";

        list.getBody().add(f.newVariableDecl(level, f.newArrayType(1, f.newRealType()), f.newSeqComp(f.newRealLiteral(0.0))));
        list.getBody().add(f.newVariableDecl(valve, f.newArrayType(1, f.newRealType()), f.newSeqComp(f.newRealLiteral(0.0))));

        list.getBody().add(setReal("s", ctrlDecl, ctrl, ints(0,1), reals(2.0,1.0)));
        list.getBody().add(setReal("s", tankDecl, tank, ints(0,1,2,3,4,5,6), reals(9.0,1.0,1.0,9.81,1.0,0.0,0.0)));

        // crtl.valve->tank.valve
        list.getBody().add(getBoolean("s", ctrlDecl, ctrl, ints(4), (f.newVariableExp(valve))));
        list.getBody().add(setReal("s", tankDecl, tank, ints(16), (f.newVariableExp(valve))));

        // tank.level -> crtl.level
        list.getBody().add(getReal("s", tankDecl, tank, ints(17), (f.newVariableExp(level))));
        list.getBody().add(setReal("s", ctrlDecl, ctrl, ints(3), (f.newVariableExp(level))));

        list.getBody().add(f.assignment("s", f.newApply(tankDecl, "exitInitializationMode", f.newVariableExp(tank))));
        list.getBody().add(f.assignment("s", f.newApply(ctrlDecl, "exitInitializationMode", f.newVariableExp(ctrl))));

        String time = "time";

        list.getBody().add(f.newVariableDecl(time, f.newRealType(), f.newRealLiteral(0.0)));

        ABlockStm whileBody = f.newBlock();
        list.getBody().add(f.newWhile(f.newLessThan(f.newVariableExp(time), f.newRealLiteral(10.0)), whileBody));

        whileBody.getBody()
                .add(f.assignment("s", f.newApply(ctrlDecl, "doStep",f.newVariableExp(ctrl), f.newVariableExp(time), f.newRealLiteral(0.001),
                        f.newBoolLiteral(false))));
        whileBody.getBody()
                .add(f.assignment("s", f.newApply(tankDecl, "doStep", f.newVariableExp(tank),f.newVariableExp(time), f.newRealLiteral(0.001),
                        f.newBoolLiteral(false))));


        whileBody.getBody().add(getReal("s", tankDecl, tank, ints(17), (f.newVariableExp(level))));
        whileBody.getBody().add(setReal("s", ctrlDecl, ctrl, ints(3), (f.newVariableExp(level))));
        whileBody.getBody().add(getBoolean("s", ctrlDecl, ctrl, ints(4), (f.newVariableExp(valve))));
        whileBody.getBody().add(setReal("s", tankDecl, tank, ints(16), f.newVariableExp(valve)));

        whileBody.getBody().add(f.assignment(time, f.newAddition(f.newVariableExp(time), f.newRealLiteral(0.001))));


        list.getBody().add(f.assignment("s", f.newApply(tankDecl, "terminate", f.newVariableExp(tank))));
        list.getBody().add( f.newCall(tankDecl, "freeInstance", f.newVariableExp(tank)));
        list.getBody().add(f.newUnload(tankDecl));

        list.getBody().add(f.assignment("s", f.newApply(ctrlDecl, "terminate", f.newVariableExp(ctrl))));
        list.getBody().add( f.newCall(ctrlDecl, "freeInstance", f.newVariableExp(ctrl)));
        list.getBody().add(f.newUnload(ctrlDecl));


        ASimulationSpecification spec = new ASimulationSpecification();
        spec.setBody(list);
        System.out.println("\n#########################################################");
        System.out.println("#                      SPEC                             #");
        System.out.println("#-------------------------------------------------------#\n ");

        System.out.println(spec.apply(new PrettyPrinter()));

        spec.apply(new CppRewriter());

        System.out.println("\n#########################################################");
        System.out.println("#                    C++ code                           #");
        System.out.println("#-------------------------------------------------------#\n");
        System.out.println(spec.apply(new SourceGenerator()));
    }

    public static int[] ints(int... v) {
        return v;
    }

    public static PExp varExps(PExp... var) {
        return f.newSeqComp(Arrays.asList(var));
    }

    public static double[] reals(double... v) {
        return v;
    }

    public static PStm setReal(String result, String decl, String comp, int[] vref, double[] values) {
        return f.assignment(result, f.newApply(decl, "setReal", f.newVariableExp(comp),
                f.newSeqComp(Arrays.stream(vref).mapToObj(v -> f.newIntLiteral(v)).collect(Collectors.toList())), f.newIntLiteral(vref.length),
                f.newSeqComp(Arrays.stream(values).mapToObj(v -> f.newRealLiteral(v)).collect(Collectors.toList()))));
    }

    public static PStm setReal(String result, String decl, String comp, int[] vref, PExp values) {
        return f.assignment(result, f.newApply(decl, "setReal", f.newVariableExp(comp),
                f.newSeqComp(Arrays.stream(vref).mapToObj(v -> f.newIntLiteral(v)).collect(Collectors.toList())), f.newIntLiteral(vref.length),
                values));
    }

    public static PStm getReal(String result, String decl, String comp, int[] vref, PExp values) {
        return f.assignment(result, f.newApply(decl, "getReal", f.newVariableExp(comp),
                f.newSeqComp(Arrays.stream(vref).mapToObj(v -> f.newIntLiteral(v)).collect(Collectors.toList())), f.newIntLiteral(vref.length),
                values));
    }

    public static PStm getBoolean(String result, String decl, String comp, int[] vref, PExp values) {
        return f.assignment(result, f.newApply(decl, "getBoolean", f.newVariableExp(comp),
                f.newSeqComp(Arrays.stream(vref).mapToObj(v -> f.newIntLiteral(v)).collect(Collectors.toList())), f.newIntLiteral(vref.length),
                values));
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

        public PStm newCall(String rootId, String functionId, PExp... args) {
            ACallStm apply = new ACallStm();
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

        public PExp newSeqComp(PExp... members) {
            return newSeqComp(Arrays.asList(members));

        }

        public PExp newSeqComp(List<? extends PExp> members) {
            ASeqCompExp comp = new ASeqCompExp();
            comp.setMembers(members);
            return comp;
        }

        public PExp newIntLiteral(int v) {
            return new AIntLiteralExp(v);

        }

        public PStm newImport(String name) {
            AImportStm im = new AImportStm();
            im.setName(newIdentifierExp(name));
            im.setImportType(1);
            return im;
        }

        public AVariableDeclarationStm newVariableDecl(String name, PType type, PExp initializer) {
            AVariableDeclarationStm decl = new AVariableDeclarationStm();
            decl.setName(newIdentifierExp(name));
            decl.setType(type);
            decl.setInitializer(initializer);
            return decl;
        }

        public SBasicType newRealType() {
            return new ARealBasicType();
        }

        public PType newArrayType(int size, SBasicType type) {
            AArrayType t = new AArrayType();
            t.setType(type);
            t.setSize(size);
            return t;
        }

        public PExp newLessThan(PExp left, PExp right) {
            ALessThanBinaryExp exp = new ALessThanBinaryExp();
            exp.setLeft(left);
            exp.setRight(right);
            return exp;
        }

        public PStm newWhile(PExp condition, PStm body) {
            AWhileStm whileStm = new AWhileStm();
            whileStm.setCondition(condition);
            whileStm.setBody(body);
            return whileStm;
        }

        public ABlockStm newBlock() {
            return new ABlockStm();
        }

        public PStm newUnload(String name) {
            AUnloadStm stm = new AUnloadStm();
            stm.setFmu(f.newIdentifierExp(name));
            return stm;
        }

        public PExp newAddition(PExp left, PExp right) {
            AAdditionBinaryExp exp = new AAdditionBinaryExp();
            exp.setLeft(left);
            exp.setRight(right);
            return exp;
        }

        public PType newNamedType(String name) {
            ANameType t = new ANameType();
            t.setName(f.newIdentifierExp(name));
            return t;
        }
    }

}
