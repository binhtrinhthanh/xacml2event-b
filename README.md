# XACML2Event-B

This repository provides the supporting artifact for the article
**“Xacml2Event-B: a formal approach for verifying XACML access control policies using Event-B.”**

It contains the Java implementation, policy examples, Event-B model listings,
experimental scripts, and result summaries. The accompanying documentation
describes how to run the tool, reproduce the finite evaluation of policy-level
combining events, and run the parser checks.

## Repository Structure

```text
.
├── src/                       Java implementation
├── models/FMSEventB/           Event-B contexts and machines
├── examples/
│   ├── FMS/                   Financial Management System examples
│   ├── HAS/                   Healthcare Access Control System examples
│   └── SIS/                   Student Information System examples
├── experiments/
│   ├── check_policy_events.py Finite evaluation of combining events
│   └── CheckPolicyParser.java  Java parser and generator checks
├── results/                   CSV summaries and parser-check output
├── docs/                      Installation, usage, and reproduction guides
├── LICENSE
└── README.md
```

## XACML2Event-B Tool

The Java implementation is available under [`src/`](src/). The tool provides
three functions:

- **Txt2Xacml** converts structured textual rule descriptions into XACML.
- **Xacml2Txt** extracts rule information from XACML into a textual representation.
- **Xacml2EvtB** generates Event-B contexts and machines from supported XACML
  policy structures.

The prototype represents a `PolicySet`–`Policy`–`Rule` hierarchy and supports
`Permit` and `Deny` rule effects. The generated machines implement
`PermitOverrides`, `DenyOverrides`, and `FirstApplicable` at the `Policy` level.
The Event-B decision domain also includes `NotApplicable`.

The generator retains XACML target and condition expressions as comments in
the generated contexts. The corresponding applicability predicates remain
abstract.

## Event-B Model

The [`models/FMSEventB/`](models/FMSEventB/) directory contains the Event-B
listings for the FMS case study:

| File | Role |
|---|---|
| `XACML_Context.ctx` | Rule effects, decision values, and abstract target and condition interpretations |
| `XACML_Refined1_Context.ctx` | Policy structure, rule membership, order, and combining algorithms |
| `XACML_AbsMachine.mch` | Abstract request evaluation and rule applicability |
| `XACML_Refined1_Machine.mch` | Policy applicability and policy-level combining events |
| `XACML_Refined2_Machine.mch` | Decision-soundness invariants |

[Table 4](results/rodin_proof_obligations.csv) summarizes the original Rodin
verification of the case-study model. Replaying that run requires the
corresponding native Rodin project and proof records.

## Running the Tool

The Java tool requires JDK 17 or later and uses only standard Java libraries.
The main class is `xacml2evtb.Xacml2EventBTool`. To compile and launch the
graphical interface from the repository root, run:

```sh
javac -encoding UTF-8 -d out src/module-info.java src/xacml2evtb/Xacml2EventBTool.java
java --module-path out -m XACML2EventB/xacml2evtb.Xacml2EventBTool
```

In the graphical interface, open the **Xacml2EvtB** tab, load an XACML policy,
and select **Convert → Event-B**. Use **Save all** to export the generated
contexts and machines. See the [installation guide](docs/installation.md)
and [usage guide](docs/usage.md) for details.

## Reproducing the Finite Combining Evaluation

From the repository root, run the following command with Python 3.9 or later:

```sh
python3 experiments/check_policy_events.py
```

The script evaluates the guards and decision assignments of eight policy-level
combining events from `XACML_Refined1_Machine.mch`. It supplies target matching
and rule applicability directly and compares the resulting decisions with a
reference evaluator. The expected results are:

- **2,040 of 2,040** decisions agree with the reference evaluator when the
  policy target matches, covering all eight combining events.
- In **2,040** cases with nonmatching policy targets, no combining event for
  the selected policy is enabled.

The script checks its output against [Table 5](results/policy_combining_validation.csv).
The [reproduction guide](docs/reproducibility.md) also provides commands for the
Java parser checks and the generator regression check.

## Experimental Results

| Article table | Result file |
|---|---|
| Table 1 — Text-to-XACML conversion | [`text_to_xacml.csv`](results/text_to_xacml.csv) |
| Table 2 — XACML-to-text conversion | [`xacml_to_text.csv`](results/xacml_to_text.csv) |
| Table 4 — Rodin proof obligations | [`rodin_proof_obligations.csv`](results/rodin_proof_obligations.csv) |
| Table 5 — Finite combining evaluation | [`policy_combining_validation.csv`](results/policy_combining_validation.csv) |

The CSVs for Tables 1, 2, and 4 record the aggregate results reported in the
article. The Python script reproduces the counts in Table 5. See the
[results index](results/README.md) for details and the
[captured Java check output](results/parser_checks.txt).

## License

See [`LICENSE`](LICENSE).
