package xacml2evtb;

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

    public static void main(String[] args) throws Exception {
        String first = policy("P1", "R1", "Permit");
        String second = policy("P2", "R2", "Deny");
        var flat = Xacml2EventBTool.XACMLParser.parseFromString(
                policySet("Root", "deny-overrides", first + second));
        String context = Xacml2EventBTool.EventBGenerator.generate(flat).ctxRef1;
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
        checkRejection(policySet("Root", "deny-overrides", policySet("Child", "deny-overrides", first)),
                "Nested PolicySet is outside the supported fragment");
        checkRejection(policySet("Root", "only-one-applicable", first),
                "Unsupported combining algorithm:");
    }
}
