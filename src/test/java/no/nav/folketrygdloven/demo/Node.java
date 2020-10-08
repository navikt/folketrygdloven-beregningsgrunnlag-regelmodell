package no.nav.folketrygdloven.demo;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.UUID;

import no.nav.fpsak.nare.doc.RuleDescription;

public class Node {

    private String id;           //Current node id, Example: 2
    private String ruleId;
    private String ruleDescription;
    private String operator;

    private LinkedHashSet<Node> children;    //Example: Nodes with IDs 3 & 4

    public Node() {
        this.children = new LinkedHashSet<>();
    }

    public Node(RuleDescription ruledesc) {
        this.setId(UUID.randomUUID().toString());
        this.setRuleId(ruledesc.getRuleIdentification());
        this.setRuleDescription(ruledesc.getRuleDescription());
        this.setOperator(ruledesc.getOperator().name());
        this.children = new LinkedHashSet<>();
    }

    public Node(String childId, String parentId) {
        this.id = childId;
        this.children = new LinkedHashSet<>();
    }

    public String getRuleId() {
        return ruleId;
    }

    public void setRuleId(String ruleId) {
        this.ruleId = ruleId;
    }

    public String getRuleDescription() {
        return ruleDescription;
    }

    public void setRuleDescription(String ruleDescription) {
        this.ruleDescription = ruleDescription;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public HashSet<Node> getChildren() {
        return children;
    }

    public void setChildren(LinkedHashSet<Node> children) {
        this.children = children;
    }

    public void addChild(Node child) {
        if (!this.children.contains(child) && child != null)
            this.children.add(child);
    }

}
