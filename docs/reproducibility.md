# Reproducibility

Run the commands below from the repository root. See [the results index](../results/README.md)
for the mapping between the CSV files and manuscript tables.

## Section 4.4: finite policy-level combining evaluation

Requirements: Python 3.9 or later.

```sh
python3 experiments/check_policy_events.py
```

The script reads the combining guards and decision assignments from
`models/FMSEventB/XACML_Refined1_Machine.mch`. It enumerates policies with one
to four rules, every assignment of Permit and Deny effects, every subset of
applicable rules, and two applicability states for a rule outside the policy.
For each of the three algorithms, it checks 680 matching-target cases against
a reference evaluator and verifies that no combining event is enabled in the
680 corresponding nonmatching-target cases.

The expected totals are 2,040 decisions in agreement, eight events covered,
and 2,040 nonmatching-target cases with no combining event enabled. The
matching-target outcomes are checked against Table 5. Target matching and rule
applicability are supplied directly. The run evaluates policy-level combining
over those states.

## Section 4.4: parser checks and generator regression check

Requirements: JDK 17 or later. The following commands build and run the checks
without opening the graphical interface.

```sh
javac -encoding UTF-8 -d out src/module-info.java src/xacml2evtb/Xacml2EventBTool.java experiments/CheckPolicyParser.java
java --module-path out -m XACML2EventB/xacml2evtb.CheckPolicyParser
```

The checks cover rejection of a nested PolicySet and rejection of a
PolicySet specifying `only-one-applicable`. The regression check accepts a
flat PolicySet with two policies and verifies that the generated rule-order
maps are restricted to each policy's own rules. The generator uses a partial
function over the global rule set and constrains its domain for each policy. The check also verifies that all eight combining
events in the generated second refinement refer to matching events in its
parent machine.

## Original conversion and proof results

The CSVs for Tables 1, 2, and 4 transcribe the aggregate results reported in
the manuscript. The JSON examples support inspection of the input formats.
Replaying the original experiments requires the original case-study inputs,
conversion records, and, for Table 4, the native Rodin project and proof
records. The distributed Event-B listings in `models/FMSEventB/` are
preserved from baseline commit `8cc1782`. The generator correction was
applied to the Java source. Table 4 reports the manuscript's original
case-study figures, before this generator correction.
