package org.intocps.maestro;

import org.maestro.ast.analysis.AnalysisException;
import org.maestro.ast.analysis.DepthFirstAnalysisAdaptor;
import org.maestro.ast.analysis.QuestionAnswerAdaptor;
import org.maestro.ast.expression.*;
import org.maestro.ast.node.INode;
import org.maestro.ast.statements.ABlockStm;
import org.maestro.ast.statements.AVariableDeclarationStm;
import org.maestro.ast.statements.PStm;
import org.maestro.ast.types.ABooleanBasicType;
import org.maestro.ast.types.AIntBasicType;
import org.maestro.ast.types.*;
import org.maestro.ast.types.PType;

import java.util.Arrays;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CppRewriter extends DepthFirstAnalysisAdaptor {

    int namePostFix;
    String namePreFix = "argName_";

    Main.Factory f = new Main.Factory();

    String getNextName() {
        return namePreFix + namePostFix++;
    }

    @Override
    public void caseAApplyExp(AApplyExp node) throws AnalysisException {


        for (int i = 0; i < node.getArgs().size(); i++) {
            PExp arg = node.getArgs().get(i);
            if (!(arg instanceof ASeqCompExp)) {
                continue;
            }
            ASeqCompExp comp = (ASeqCompExp) arg;
            //not allowed
            String name = getNextName();

            SBasicType argType = getType(comp);

            String fname = node.getFunctionName().getValue();
            if (Stream.of("Real", "Boolean", "Integer", "String").map(t -> Stream.of("get" + t, "set" + t)).flatMap(Function.identity())
                    .collect(Collectors.toSet()).contains(fname) && i == 1) {
                argType = new AUnsingedIntBasicType();
            }

            AVariableDeclarationStm variable = f.newVariableDecl(name, f.newArrayType(comp.getMembers().size(), argType.clone()), null);
            node.getArgs().set(i, f.newIdentifierExp(name));
            variable.setInitializer(arg);
            //node.getAncestor(ABlockStm.class).getBody().add(0,variable);

            PStm containingStm = node.getAncestor(PStm.class);
            ABlockStm block = containingStm.getAncestor(ABlockStm.class);

            int index = block.getBody().indexOf(containingStm);
            block.getBody().add(index, variable);

        }

    }

    private SBasicType getType(ASeqCompExp comp) {
        PExp elem = comp.getMembers().getFirst();
        if (elem instanceof ARealLiteralExp) {
            return new ARealBasicType();
        } else if (elem instanceof AIntLiteralExp) {
            return new AIntBasicType();
        } else if (elem instanceof ABooleanLiteralExp) {
            return new ABooleanBasicType();
        } else if (elem instanceof AStringLiteralExp) {
            return new AStringBasicType();
        } else if (elem instanceof AVariableExp) {

            ABlockStm block = elem.getAncestor(ABlockStm.class);

            while (block != null) {

                for (PStm stm : block.getBody()) {

                    if (stm instanceof AVariableDeclarationStm && ((AVariableDeclarationStm) stm).getName().getValue()
                            .equals(((AVariableExp) elem).getName().getValue())) {
                        PType t = ((AVariableDeclarationStm) stm).getType();
                        if (t instanceof AArrayType) {
                            return ((AArrayType) t).getType();
                        } else if (t instanceof SBasicType) {
                            return (SBasicType) t;
                        }
                    }

                }
                block = block.parent().getAncestor(ABlockStm.class);


            }
        }
        return null;
    }


}
