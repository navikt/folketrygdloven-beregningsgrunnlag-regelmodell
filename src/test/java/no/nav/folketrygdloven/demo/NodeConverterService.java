package no.nav.folketrygdloven.demo;

import java.util.IdentityHashMap;

import no.nav.fpsak.nare.doc.RuleDescription;
import no.nav.fpsak.nare.evaluation.Operator;

public class NodeConverterService {

    private static transient IdentityHashMap<RuleDescription, Boolean> processed = new IdentityHashMap<>();

    public static Node convert(RuleDescription ruleDesc) {

        return convert(null, ruleDesc);
    }

    private static Node convert(Node parent, RuleDescription ruleDesc) {
        Boolean prev = processed.putIfAbsent(ruleDesc, true);
        if (prev != null) {
            return null;
        }

        Node childNode = new Node(ruleDesc);

        if (parent == null) {
            parent = childNode;
        }

        if (ruleDesc.getOperator() == Operator.COMPUTATIONAL_IF) {
            processCondIf(childNode, ruleDesc);
        } else {
            ruleDesc.getChildren().forEach(e -> convert(childNode, e));
        }

        if (parent != childNode)
            parent.addChild(childNode);

        return childNode;
    }

    private static void processCondIf(Node ifNode, RuleDescription ruleDesc) {
        int index = 0;
        for (RuleDescription child : ruleDesc.getChildren()) {
            if (index == 0 && child.getOperator() == Operator.SINGLE) {
                ifNode.setRuleId(child.getRuleIdentification());
                ifNode.setRuleDescription(child.getRuleDescription());
            } else {
                convert(ifNode, child);
            }
            index++;
        }
    }
}
