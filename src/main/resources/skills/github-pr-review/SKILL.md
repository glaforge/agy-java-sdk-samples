---
name: github-pr-review
description: Expert guidelines, rubrics, and workflows for analyzing, reviewing, and comparing GitHub Pull Requests. Use when evaluating competing or complementary PRs, inspecting code diffs, verifying test quality, assessing backwards compatibility, and formulating merge recommendations.
---

# GitHub Pull Request Review & Comparison Skill

This skill provides a systematic rubric and workflow for analyzing individual pull requests and performing head-to-head comparisons of competing PRs that solve the same underlying feature request or bug.

---

## 1. Multi-Agent Delegation Strategy

When reviewing two or more competing PRs, consider leveraging **subagents** (`invoke_subagent`) to perform parallel analyses:
- **Subagent 1**: Focus exclusively on PR #1 (diff inspection, architectural choices, test cases, and edge cases).
- **Subagent 2**: Focus exclusively on PR #2 (diff inspection, architectural choices, test cases, and edge cases).
- **Coordinator (Lead Agent)**: Ingests the two reviews, performs the synthesis, resolves contradictions, and delivers the final recommendation.

---

## 2. PR Review Rubric

When evaluating any pull request, systematically evaluate these 5 dimensions:

### A. Problem Statement & Root Cause Alignment
- Does the PR correctly identify and address the issue/feature requirement?
- Does it treat the root cause or just suppress a symptom?
- Does it align with the parent project's design philosophy and module boundaries?

### B. Implementation Soundness & Design
- **Data structures & models**: How are new payloads or entities modeled?
- **Separation of concerns**: Are changes localized to the appropriate classes/packages without leaking internal details?
- **Defensive programming**: Are null checks, missing fields, or invalid formats handled gracefully?
- **Streaming & Concurrency**: In streaming contexts (e.g. LLM chunk streams), are chunks properly accumulated, concatenated, and finalized across events?

### C. API Design & Backwards Compatibility
- Does the change break any existing public interfaces, method signatures, or return types?
- Are new attributes accessible without forcing existing callers to rewrite their code?
- Are keys or constants clearly defined (e.g., public constants vs magic strings)?

### D. Test Coverage & Quality
- Are there unit tests that isolate the new behavior?
- Do tests avoid unnecessary external dependencies (e.g. not requiring live API keys or remote network calls)?
- Are edge cases tested (empty payloads, multiple items, streaming chunks, unexpected MIME types)?
- Are integration / serialization profiles covered (e.g., Jackson, JSON)?

### E. Code Hygiene & Documentation
- Are Javadoc / docstrings provided for new public methods and constants?
- Is code formatted according to repository standards?
- Are commit messages and PR descriptions informative?

---

## 3. Comparative Synthesis Framework

When comparing two PRs (e.g., PR A vs PR B) that solve the same issue:

1. **Executive Summary**: Brief statement of the problem and the high-level approach taken by each PR.
2. **Side-by-Side Comparison Matrix**:
   | Feature / Dimension | PR A (#...) | PR B (#...) | Advantage |
   | :--- | :--- | :--- | :--- |
   | Core Implementation | ... | ... | ... |
   | Streaming Support | ... | ... | ... |
   | Error / MIME Handling | ... | ... | ... |
   | Test Coverage | ... | ... | ... |
   | Backwards Compatibility | ... | ... | ... |
3. **Key Trade-offs**: Highlight where each author made different engineering trade-offs.
4. **Synergies & Complementary Ideas**: Identify whether ideas from one PR should be adopted by the other (e.g. PR A has better streaming support, while PR B has cleaner defensive MIME type validation).
5. **Final Recommendation**: Clear decision on which PR to accept, merge, or how to combine their strengths.
