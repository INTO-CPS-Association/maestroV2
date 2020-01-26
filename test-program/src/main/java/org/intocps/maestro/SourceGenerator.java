package org.intocps.maestro;

import org.maestro.ast.analysis.AnalysisException;
import org.maestro.ast.analysis.AnswerAdaptor;
import org.maestro.ast.expression.*;
import org.maestro.ast.node.INode;
import org.maestro.ast.node.NodeList;
import org.maestro.ast.specification.ASimulationSpecification;
import org.maestro.ast.statements.*;
import org.maestro.ast.types.*;
import org.maestro.ast.types.ABooleanBasicType;
import org.maestro.ast.types.AIntBasicType;
import org.maestro.ast.types.ARealBasicType;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


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
        return String.format("while(%s)\n%s", node.getCondition().apply(this), node.getBody().apply(this));
    }

    @Override
    public String caseASimulationSpecification(ASimulationSpecification node) throws AnalysisException {

        String funnctions = " fmi2CallbackFunctions callback = {.logger = &fmuLogger, .allocateMemory = calloc, .freeMemory = free, " + ".stepFinished = " + "NULL, .componentEnvironment = NULL};\n";

        return funnctions + node.getBody().apply(this);

    }

    @Override
    public String caseACallStm(ACallStm node) throws AnalysisException {
        return caseAApplyExp(node.getRoot(),node.getFunctionName(),node.getArgs());
    }

    @Override
    public String caseAApplyExp(AApplyExp node) throws AnalysisException {

       return caseAApplyExp(node.getRoot(),node.getFunctionName(),node.getArgs());

    }

    public String caseAApplyExp(PExp root, AIdentifierExp functionName, List<PExp> args) throws AnalysisException {

        if (functionName.getValue().equals("instantiate")) {
            String name = args.get(0).apply(this);
            String uuid = args.get(1).apply(this);
            String uri = args.get(2).apply(this);
            String visible = args.get(3).apply(this);
            String logginOn = args.get(4).apply(this);

            return String
                    .format("%s.instantiate(%s,fmi2CoSimulation,%s,%s,&%s,%s,%s)", root.apply(this), name, uuid, uri, "callback", visible,
                            logginOn);
        }

        String ret = root.apply(this) + "." + functionName.apply(this) + "(";
        Iterator<PExp> itr = args.iterator();
        while (itr.hasNext()) {
            ret += itr.next().apply(this);
            if (itr.hasNext()) {
                ret += ",";
            }
        }
        return ret + ")";

    }

    @Override
    public String caseAUnsingedIntBasicType(AUnsingedIntBasicType node) throws AnalysisException {
        return "unsigned int";
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
    public String caseAIntLiteralExp(AIntLiteralExp node) throws AnalysisException {
        return node.getValue() + "";
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
        String name = node.getName().apply(this);

        if (name.equals("fmi2")) {
            return "FMU";
        } else {
            return name;
        }
    }

    @Override
    public String caseAVariableDeclarationStm(AVariableDeclarationStm node) throws AnalysisException {

        String res = "";
        if (node.getType() instanceof AArrayType) {
            AArrayType aType = (AArrayType) node.getType();
            res = aType.getType().apply(this) + " " + node.getName().apply(this) + "[" + aType.getSize() + "]";
        } else {


            res = node.getType().apply(this) + " " + node.getName().apply(this);

        }
        if (node.getInitializer() != null) {
            res += (" = " + node.getInitializer().apply(this));
        }

        return res;
    }


    @Override
    public String caseAUnloadStm(AUnloadStm node) throws AnalysisException {
        return "//unlooad " + node.getFmu().apply(this);
    }

    @Override
    public String caseALoadStm(ALoadStm node) throws AnalysisException {
        return "auto " + node.getTarget().getValue().get(0).apply(this) + " = loadDll(" + node.getUri().apply(this) + "," + "&" + node.getTarget()
                .getValue().get(1).apply(this) + ")";
    }

    @Override
    public String caseASeqCompExp(ASeqCompExp node) throws AnalysisException {
        List<String> membersGenerated = new ArrayList<>();
        for (PExp exp : node.getMembers()) {
            membersGenerated.add(exp.apply(this));
        }


        return "{" + String.join(",", membersGenerated) + "}";
    }
}
