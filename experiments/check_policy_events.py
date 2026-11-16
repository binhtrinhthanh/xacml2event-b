"""Reproduce Table 5 by interpreting the combining events on finite cases."""

import csv
import re
from collections import Counter
from itertools import product
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MACHINE = ROOT / "models/FMSEventB/XACML_Refined1_Machine.mch"
RESULTS = ROOT / "results/policy_combining_validation.csv"
ALGORITHMS = ("PermitOverrides", "DenyOverrides", "FirstApplicable")
OUTCOMES = ("Permit", "Deny", "NotApplicable")


def read_events():
    source = MACHINE.read_text(encoding="utf-8")
    pattern = (
        r"^\s*⚬\s*((?:Combine_|ApplyCombining_)\w+)\s*:[^\n]*\n"
        r"(.*?)^\s*END\s*$"
    )
    events = []
    for name, body in re.findall(pattern, source, re.M | re.S):
        guards = tuple(
            "".join(guard.split())
            for guard in re.findall(r"⚬\s*grd\d+:\s*(.*?)\s+not theorem\s*›", body, re.S)
        )
        action, = re.findall(r"decision\s*≔\s*(Permit|Deny|NotApplicable|effect\(r\))\s*›", body)
        events.append((name, guards, action))
    if len(events) != 8:
        raise ValueError(f"Expected eight combining events, found {len(events)}")
    return events


def enabled_events(events, algorithm, effects, applicable, target_matches):
    # Policy rule IDs follow their order. Any additional ID lies outside the policy.
    policy_rules = range(len(effects))
    selected = applicable.intersection(policy_rules)

    def evaluate(guard, rule):
        prefix = "pol∈applicable_policies∧combining_alg_policy(pol)="
        if guard.startswith(prefix):
            return target_matches and guard.removeprefix(prefix) == algorithm
        for effect in OUTCOMES[:2]:
            exists = f"∃r·r∈applicable_rules∧r∈policies(pol)∧effect(r)={effect}"
            if guard == exists:
                return any(effects[index] == effect for index in selected)
            if guard == f"¬({exists})":
                return all(effects[index] != effect for index in selected)
        if guard == "applicable_rules∩policies(pol)=∅":
            return not selected
        if guard == "r∈applicable_rules∧r∈policies(pol)":
            return rule in selected
        if guard == (
            "∀s·s∈policies(pol)∧s∈applicable_rules⇒rule_order(pol)(r)≤rule_order(pol)(s)"
        ):
            return all(rule <= other for other in selected)
        raise ValueError(f"Unsupported guard: {guard}")

    for name, guards, action in events:
        parameters = policy_rules if action == "effect(r)" else (None,)
        for rule in parameters:
            if all(evaluate(guard, rule) for guard in guards):
                yield name, effects[rule] if action == "effect(r)" else action


def reference(algorithm, outcomes):
    if algorithm == "PermitOverrides":
        return next((effect for effect in OUTCOMES if effect in outcomes), "NotApplicable")
    if algorithm == "DenyOverrides":
        return next((effect for effect in ("Deny", "Permit") if effect in outcomes), "NotApplicable")
    return next((effect for effect in outcomes if effect != "NotApplicable"), "NotApplicable")


def main():
    events = read_events()
    counts = Counter()
    matching = Counter()
    nonmatching = 0
    covered = set()
    for size in range(1, 5):
        for effects in product(OUTCOMES[:2], repeat=size):
            for mask in product((False, True), repeat=size + 1):
                applicable = {rule for rule, applies in enumerate(mask) if applies}
                rule_outcomes = tuple(
                    effect if rule in applicable else "NotApplicable"
                    for rule, effect in enumerate(effects)
                )
                for algorithm in ALGORITHMS:
                    enabled = list(enabled_events(events, algorithm, effects, applicable, True))
                    expected = reference(algorithm, rule_outcomes)
                    if len(enabled) != 1 or enabled[0][1] != expected:
                        raise AssertionError((algorithm, effects, mask, enabled, expected))
                    event, decision = enabled[0]
                    covered.add(event)
                    counts[algorithm, decision] += 1
                    matching[algorithm] += 1
                    if any(enabled_events(events, algorithm, effects, applicable, False)):
                        raise AssertionError(("Nonmatching policy target", algorithm, effects, mask))
                    nonmatching += 1

    rows = {}
    for algorithm in ALGORITHMS:
        rows[algorithm] = tuple(counts[algorithm, outcome] for outcome in OUTCOMES) + (
            matching[algorithm], matching[algorithm],
        )
    rows["Total"] = tuple(map(sum, zip(*rows.values())))
    with RESULTS.open(encoding="utf-8", newline="") as file:
        reader = csv.reader(file)
        print(",".join(next(reader)))
        expected_rows = {name: tuple(map(int, values)) for name, *values in reader}
    if rows != expected_rows or len(covered) != 8 or nonmatching != 2040:
        raise AssertionError((rows, expected_rows, covered, nonmatching))
    for algorithm, values in rows.items():
        print(",".join(map(str, (algorithm, *values))))
    print(f"Events covered: {len(covered)}")
    print(f"Nonmatching target cases with no combining event enabled: {nonmatching}")


if __name__ == "__main__":
    main()
