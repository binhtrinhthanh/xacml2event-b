package xacml2evtb;

import java.util.regex.Pattern;

/** Exercises the Java parser and generator without opening the Swing interface. */
public final class CheckPolicyParser {
    private static final String FIRST = "urn:oasis:names:tc:xacml:1.0:rule-combining-algorithm:first-applicable";

    private static String policy(String id, String rule, String effect) {
        return "<Policy PolicyId='" + id + "' RuleCombiningAlgId='" + FIRST + "'>"
                + "<Target/><Rule RuleId='" + rule + "' Effect='" + effect + "'/></Policy>";
    }

    private static String policySet(String id, String algorithm, String children) {
        return "<PolicySet xmlns='urn:oasis:names:tc:xacml:3.0:core:schema:wd-17' PolicySetId='"
                + id + "' PolicyCombiningAlgId='urn:oasis:names:tc:xacml:1.0:policy-combining-algorithm:"
                + algorithm + "'><Target/>" + children + "</PolicySet>";
    }

    private static void checkRejection(String xml, String expectedMessage) throws Exception {
        try {
            Xacml2EventBTool.XACMLParser.parseFromString(xml);
        } catch (IllegalArgumentException error) {
            if (error.getMessage().startsWith(expectedMessage)) {
                System.out.println("PASS " + expectedMessage);
                return;
            }
            throw error;
        }
        throw new AssertionError("Input unexpectedly accepted: " + expectedMessage);
    }

    private static void checkCombiningRefinement(Xacml2EventBTool.EventBModel model) {
        var combiningEvents = Pattern.compile(
                "(?m)^    ((?:Combine_|ApplyCombining_)\\w+)\\n"
                + "        REFINES (\\w+)\\n").matcher(model.ref2Mch);
        int checked = 0;
        while (combiningEvents.find()) {
            String event = combiningEvents.group(1);
            String parent = combiningEvents.group(2);
            if (!event.equals(parent) || !model.ref1Mch.contains("\n    " + parent + "\n")) {
                throw new AssertionError("Invalid refinement for " + event + ": " + parent);
            }
            checked++;
        }
        if (checked != 8) {
            throw new AssertionError("Expected eight combining event refinements, found " + checked);
        }
        System.out.println("PASS Eight combining events refine their matching parent events");
    }

    public static void main(String[] args) throws Exception {
        String first = policy("P1", "R1", "Permit");
        String second = policy("P2", "R2", "Deny");
        var flat = Xacml2EventBTool.XACMLParser.parseFromString(
                policySet("Root", "deny-overrides", first + second));
        var generated = Xacml2EventBTool.EventBGenerator.generate(flat);
        String context = generated.ctxRef1;
        for (String expected : new String[] {
                "rule_order ∈ POLICY → (RULE ⇸ ℕ)",
                "dom(rule_order(pol)) = policies(pol)",
                "rule_order(P1) = {R1 ↦ 1}",
                "rule_order(P2) = {R2 ↦ 1}"
        }) {
            if (!context.contains(expected)) {
                throw new AssertionError("Missing generated axiom: " + expected);
            }
        }
        System.out.println("PASS Flat PolicySet and per-policy rule order");
        checkCombiningRefinement(generated);
        checkRejection(policySet("Root", "deny-overrides", policySet("Child", "deny-overrides", first)),
                "Nested PolicySet is outside the supported fragment");
        checkRejection(policySet("Root", "only-one-applicable", first),
                "Unsupported combining algorithm:");
    }
}
