package org.intocps.maestro;

import org.maestro.ast.analysis.AnalysisException;
import org.maestro.ast.analysis.AnswerAdaptor;
import org.maestro.ast.expression.*;
import org.maestro.ast.node.INode;
import org.maestro.ast.specification.ASimulationSpecification;
import org.maestro.ast.statements.*;
import org.maestro.ast.types.*;
import org.maestro.ast.types.ABooleanBasicType;
import org.maestro.ast.types.AIntBasicType;
import org.maestro.ast.types.ARealBasicType;

import java.util.Iterator;



public class SourceGenerator extends AnswerAdaptor<String> {
    @Override
    public String createNewReturnValue(INode node) throws AnalysisException {
        return "";
    }

    @Override
    public String createNewReturnValue(Object node) throws AnalysisException {
        return "";
    }

    @Override
    public String defaultSBinaryExp(SBinaryExp node) throws AnalysisException {
        return node.getLeft().apply(this) + " + " + node.getRight().apply(this);
    }

    @Override
    public String caseAWhileStm(AWhileStm node) throws AnalysisException {
        return String.format("white(%s)\n%s", node.getCondition().apply(this), node.getBody().apply(this));
    }

    @Override
    public String caseASimulationSpecification(ASimulationSpecification node) throws AnalysisException {

        return node.getBody().apply(this);

    }



    @Override
    public String caseAApplyExp(AApplyExp node) throws AnalysisException {

        if (node.getFunctionName().getValue().equals("instantiate")) {
            String name = node.getArgs().get(0).apply(this);
            String uuid = node.getArgs().get(1).apply(this);
            String uri = node.getArgs().get(2).apply(this);
            String visible = node.getArgs().get(3).apply(this);
            String logginOn = node.getArgs().get(4).apply(this);

            //            String functionsCbName = name.replaceAll("[^a-zA-Z0-9]", "")+"func";
            //            String funnctions = " fmi2CallbackFunctions "+functionsCbName+" = {.logger = &fmuLogger, .allocateMemory = NULL, .freeMemory = NULL, " +
            //                    ".stepFinished = " +
            //                    "NULL, .componentEnvironment = NULL};";

            return String.format("%s->instantiate(%s,fmi2CoSimulation,%s,%s,&%s,%s,%s)",node.getRoot().apply(this), name, uuid, uri, "callback",
                    visible,
                    logginOn);
        }

            /*
    auto comp = fmu.instantiate("name", fmi2CoSimulation, "jkkk",
                                "file:/Users/kgl/data/au/into-cps-association/rabbitmq-fmu/fmu-loader/tmp/resources", &cbFuncs, true,
                                true);

    fmu.enterInitializationMode(comp);


    fmi2ValueReference refs[1]={};
    fmi2Real reals[1];
    fmu.setReal(comp,refs,1,reals);*/


        String ret = node.getRoot().apply(this) + "." + node.getFunctionName().apply(this) + "(";
        Iterator<PExp> itr = node.getArgs().iterator();
        while (itr.hasNext()) {
            ret += itr.next().apply(this);
            if (itr.hasNext()) {
                ret += ",";
            }
        }
        return ret + ")";

    }

    @Override
    public String caseAAssignmentStm(AAssignmentStm node) throws AnalysisException {


        return node.getTarget().apply(this) + " = " + node.getExp().apply(this);


    }

    @Override
    public String caseAIdentifierExp(AIdentifierExp node) throws AnalysisException {
        return node.getValue();
    }

    @Override
    public String caseAImportStm(AImportStm node) throws AnalysisException {
        return "//import " + node.getName();
    }

    @Override
    public String caseAArrayType(AArrayType node) throws AnalysisException {
        return node.getType().apply(this) + "[" + node.getSize() + "]";
    }

    @Override
    public String caseARealBasicType(ARealBasicType node) throws AnalysisException {
        return "double";
    }

    @Override
    public String caseAIntBasicType(AIntBasicType node) throws AnalysisException {
        return "int";
    }

    @Override
    public String caseARealLiteralExp(ARealLiteralExp node) throws AnalysisException {
        return node.getValue() + "";
    }

    @Override
    public String caseABooleanBasicType(ABooleanBasicType node) throws AnalysisException {
        return "bool";
    }

    @Override
    public String caseABooleanLiteralExp(ABooleanLiteralExp node) throws AnalysisException {
        return node.getValue() + "";
    }

    @Override
    public String caseAVariableExp(AVariableExp node) throws AnalysisException {
        return node.getName().apply(this);
    }

    @Override
    public String caseAStringLiteralExp(AStringLiteralExp node) throws AnalysisException {
        return "\"" + node.getValue() + "\"";
    }


    @Override
    public String caseABlockStm(ABlockStm node) throws AnalysisException {

        String ret = "{";
        Iterator<PStm> itr = node.getBody().iterator();
        while (itr.hasNext()) {
            ret += itr.next().apply(this);
                ret += ";\n";
        }
        return ret + "}";
    }

    @Override
    public String caseANameType(ANameType node) throws AnalysisException {
        String name =  node.getName().apply(this);

        if(name.equals("fmi2"))
            return "FMU";
        else
            return name;
    }

    @Override
    public String caseAVariableDeclarationStm(AVariableDeclarationStm node) throws AnalysisException {
        String res = node.getType().apply(this) + " " + node.getName().apply(this);

        if (node.getInitializer() != null) {
            res += (" = " + node.getInitializer().apply(this));
        }

        return res;
    }

    @Override
    public String caseAUnloadStm(AUnloadStm node) throws AnalysisException {
        return "//unlooad "+node.getFmu().apply(this);
    }

    @Override
    public String caseALoadStm(ALoadStm node) throws AnalysisException {
       return "auto "+node.getTarget().getValue().get(0).apply(this)+" = loadDll("+node.getUri().apply(this)+","+
        "&"+node.getTarget().getValue().get(1).apply(this)+
                ")";
    }
}
