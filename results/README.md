# Section 4 results

| Manuscript table | CSV | Source |
|---|---|---|
| Table 1 | [text_to_xacml.csv](text_to_xacml.csv) | Reported conversion counts |
| Table 2 | [xacml_to_text.csv](xacml_to_text.csv) | Reported conversion counts |
| Table 4 | [rodin_proof_obligations.csv](rodin_proof_obligations.csv) | Reported Rodin counts for the case-study model |
| Table 5 | [policy_combining_validation.csv](policy_combining_validation.csv) | Finite evaluation summary, checked by the script below |

The CSVs for Tables 1, 2, and 4 transcribe the manuscript's aggregate results.
Blank rates in Table 4 indicate zero generated obligations. Replaying the original
conversion and proof runs requires the corresponding experimental inputs and
Rodin proof project. The CSVs are summaries of those runs.

## Reproduce Table 5

From the repository root, with Python 3.9 or later:

```sh
python3 experiments/check_policy_events.py
```

The script reads guards and decision assignments from
[`XACML_Refined1_Machine.mch`](../models/FMSEventB/XACML_Refined1_Machine.mch)
and checks the counts against the Table 5 CSV. It uses only the Python standard
library. Expected results are 2,040 agreements across all eight combining
events and 2,040 additional nonmatching-target cases with no event enabled.
The latter cases are outside Table 5.

Target matching and rule applicability are supplied directly. The comparison
uses a Python reference evaluator for policy-level combining. The two
separate parser checks mentioned in Section 4.4 concern the Java prototype.
See [the reproduction guide](../docs/reproducibility.md) for the Java commands
and [parser_checks.txt](parser_checks.txt) for the captured output. Table 4
reports the original model results, before the Java generator correction.
