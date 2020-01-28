package org.intocps.maestro;

import org.maestro.ast.analysis.AnalysisException;
import org.maestro.ast.analysis.AnswerAdaptor;
import org.maestro.ast.expression.*;
import org.maestro.ast.node.INode;
import org.maestro.ast.specification.ASimulationSpecification;
import org.maestro.ast.statements.*;
import org.maestro.ast.types.*;

import java.util.stream.Collectors;

public class PrettyPrinter extends AnswerAdaptor<String> {
    @Override
    public String createNewReturnValue(INode node) throws AnalysisException {
        return "";
    }

    @Override
    public String createNewReturnValue(Object node) throws AnalysisException {
        return "";
    }

    @Override
    public String caseAUnsingedIntBasicType(AUnsingedIntBasicType node) throws AnalysisException {
        return "unsigned int";
    }

    @Override
    public String caseANameType(ANameType node) throws AnalysisException {
        return node.getName().apply(this);
    }

    @Override
    public String caseAIdentifierExp(AIdentifierExp node) throws AnalysisException {
        return node.getValue();
    }

    @Override
    public String caseALessThanBinaryExp(ALessThanBinaryExp node) throws AnalysisException {
        return node.getLeft().apply(this) + " < " + node.getRight().apply(this);
    }

    @Override
    public String caseAAdditionBinaryExp(AAdditionBinaryExp node) throws AnalysisException {
        return node.getLeft().apply(this) + " + " + node.getRight().apply(this);
    }

    @Override
    public String caseAUnloadStm(AUnloadStm node) throws AnalysisException {
        return "unload(" + node.getFmu().apply(this) + ")";
    }

    @Override
    public String caseAVariableDeclarationStm(AVariableDeclarationStm node) throws AnalysisException {
        return node.getType().apply(this) + " " + node.getName().apply(this) + (node.getInitializer() != null ? " = "+node.getInitializer()
                .apply(this) : "");
    }

    @Override
    public String caseAArrayType(AArrayType node) throws AnalysisException {
        return node.getType().apply(this)+"["+node.getSize()+"]";
    }

    @Override
    public String caseARealBasicType(ARealBasicType node) throws AnalysisException {
        return "real";
    }

    @Override
    public String caseASimulationSpecification(ASimulationSpecification node) throws AnalysisException {
        return node.getBody().apply(this);
    }

    @Override
    public String caseAWhileStm(AWhileStm node) throws AnalysisException {
        return "while( " + node.getCondition().apply(this) +")\n" + node.getBody().apply(this);
    }

    @Override
    public String caseABlockStm(ABlockStm node) throws AnalysisException {
        return node.getBody().stream().map(s -> {
            try {
                return s.apply(this);
            } catch (AnalysisException e) {
                e.printStackTrace();
                return "ERROR";
            }
        }).collect(Collectors.joining(";\n", "{\n", ";\n}"));
    }

    @Override
    public String caseACallStm(ACallStm node) throws AnalysisException {
        return node.getRoot().apply(this) + "." + node.getFunctionName() + "(" + node.getArgs().stream().map(a -> {
            try {
                return a.apply(this);
            } catch (AnalysisException e) {
                e.printStackTrace();
                return "?";
            }
        }).collect(Collectors.joining(",")) + ")";
    }

    @Override
    public String caseASeqCompExp(ASeqCompExp node) throws AnalysisException {
        return node.getMembers().stream().map(s-> {
            try {
                return s.apply(this);
            } catch (AnalysisException e) {
                e.printStackTrace();
                return "ERROR";
            }
        }).collect(Collectors.joining(",","{","}"));
    }

    @Override
    public String caseAIntLiteralExp(AIntLiteralExp node) throws AnalysisException {
        return node.getValue()+"";
    }

    @Override
    public String caseAApplyExp(AApplyExp node) throws AnalysisException {
        return node.getRoot().apply(this) + "." + node.getFunctionName() + "(" + node.getArgs().stream().map(a -> {
            try {
                return a.apply(this);
            } catch (AnalysisException e) {
                e.printStackTrace();
                return "?";
            }
        }).collect(Collectors.joining(",")) + ")";
    }

    @Override
    public String caseAVariableExp(AVariableExp node) throws AnalysisException {
        return node.getName().apply(this);
    }

    @Override
    public String caseALoadStm(ALoadStm node) throws AnalysisException {

        return node.getTarget().getValue().getFirst().apply(this)+ " = load("+node.getUri().apply(this)+","+node.getImportName()+","+node.getTarget().getValue().get(1)+")";
    }

    @Override
    public String caseAStringLiteralExp(AStringLiteralExp node) throws AnalysisException {
        return "\""+node.getValue()+"\"";
    }


    @Override
    public String caseABooleanLiteralExp(ABooleanLiteralExp node) throws AnalysisException {
        return node.getValue()+"";
    }

    @Override
    public String caseARealLiteralExp(ARealLiteralExp node) throws AnalysisException {
        return node.getValue()+"";
    }

    @Override
    public String caseAAssignmentStm(AAssignmentStm node) throws AnalysisException {
        return node.getTarget().apply(this)+  " = "+node.getExp().apply(this);
    }

    @Override
    public String caseAImportStm(AImportStm node) throws AnalysisException {
        String s= "import ";
        switch (node.getImportType())
        {
            case 1:
                s+="native ";
                break;
        }
        return s+ "\""+node.getName()+"\"";
    }
}
