# Skill: Execute a Java 25 Upgrade Milestone

Use this skill when asked to implement a specific milestone (2-10) of the Java 25 upgrade on the `java-25-target` branch.

## Prerequisites

- The `java-25-target` branch exists with `REVIEW.md` at the repo root
- All prior milestones have been merged into `java-25-target`
- The correct JDK is installed for the target milestone (see AGENTS.md for version mapping)

## Procedure

### Step 1: Read the milestone spec
```
Read REVIEW.md and locate the milestone section you've been asked to implement.
Note the exact scope, files list, and review criteria checkboxes.
```

### Step 2: Read the agent guidelines
```
Read AGENTS.md for branch naming, verification commands, coding standards, and common pitfalls.
```

### Step 3: Create a feature branch
```bash
git fetch origin
git checkout java-25-target
git pull origin java-25-target
git checkout -b devin/$(date +%s)-milestone-<N>-<short-description>
```
Replace `<N>` with the milestone number and `<short-description>` with a kebab-case summary.

### Step 4: Implement the changes
- Only modify files listed in the milestone scope
- Follow `.windsurf/rules/java-developer-guide.md` coding standards
- For Milestones 4+5: these MUST be combined in a single branch/PR since javax→jakarta won't compile without Spring Boot 3

### Step 5: Run verification
```bash
# Set JAVA_HOME appropriate for the milestone
export JAVA_HOME=<path-to-correct-jdk>

# Format code
./gradlew spotlessJavaApply

# Run full test suite
./gradlew clean test

# Verify coverage threshold
./gradlew jacocoTestReport jacocoTestCoverageVerification

# Run spotless check (should pass after apply)
./gradlew spotlessCheck
```

### Step 6: Validate review criteria
Go through each checkbox in the milestone's "Review criteria" section in REVIEW.md. Verify each one explicitly:
- Run the grep/search commands listed (e.g., `grep -r "org.joda.time" src/` for Milestone 3)
- Confirm version numbers in `build.gradle` match expectations
- Confirm `.java-version` and CI workflow versions match

### Step 7: Commit and push
```bash
git add -A
git commit -m "milestone <N>: <description matching REVIEW.md scope>"
git push origin HEAD
```

### Step 8: Create PR
- **Base branch:** `java-25-target` (NOT `main`)
- **Title:** `Milestone <N>: <scope summary>`
- **Body:** Include the review criteria checklist from REVIEW.md with each item checked off, plus a summary of what changed and any notable decisions

### Step 9: Wait for CI
- Monitor CI checks on the PR
- If tests fail, fix issues and push additional commits
- Do not merge — wait for human review
