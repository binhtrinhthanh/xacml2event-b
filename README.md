# XACML2Event-B

Experimental artifact for **Xacml2Event-B: a formal approach for verifying
XACML access control policies using Event-B**.

## Reproduce the R2 combining evaluation

From the repository root, with Python 3.9 or later, run:

```sh
python3 experiments/check_policy_events.py
```

The script evaluates the guards and decision assignments of eight policy-level
combining events for finite applicability states and checks the resulting counts
against [Table 5](results/policy_combining_validation.csv). It reports 2,040
agreements for matching policy targets and 2,040 nonmatching-target cases with
no combining event enabled. See [the reproduction guide](docs/reproducibility.md)
for the Java parser checks and [the results index](results/README.md) for the CSVs.

## Repository contents

| Directory | Contents |
|---|---|
| [src/](src/) | Java prototype |
| [models/FMSEventB/](models/FMSEventB/) | Distributed Event-B model listings |
| [examples/](examples/) | Example data for FMS, HACS (under `HAS/`), and SIS |
| [experiments/](experiments/) | Finite combining evaluation and Java parser checks |
| [results/](results/) | Manuscript result summaries and parser-check output |
| [docs/](docs/) | Installation, usage, and reproduction instructions |

## Run the Java prototype

The Java prototype uses standard libraries and requires JDK 17 or later.
[Installation](docs/installation.md) and [usage](docs/usage.md) describe how to
build and run the graphical interface.

- `Txt2Xacml` converts structured textual rules to XML.
- `Xacml2Txt` extracts rule information from XML.
- `Xacml2EvtB` generates Event-B model structure from the supported policy input.
  Target and condition expressions are retained as comments, and their
  interpretations remain abstract. The combining events operate at policy level.

## Rodin verification

The [results index](results/README.md) identifies the manuscript's recorded
Rodin results. The Event-B listings are available for inspection. Replaying
the original Rodin run requires the corresponding native project and proof
records.
