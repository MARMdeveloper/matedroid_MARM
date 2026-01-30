---
name: feature
description: Implement a new feature following the standard workflow. Explores the codebase, creates a plan, implements the feature, runs tests, creates a PR, and installs on the connected device.
allowed-tools: Read, Edit, Write, Bash, Glob, Grep, Task, EnterPlanMode, ExitPlanMode, AskUserQuestion, mcp__github__issue_read, mcp__github__add_issue_comment, mcp__github__create_pull_request, mcp__github__pull_request_read, mcp__github__update_pull_request, mcp__github__list_issues
---

# Feature Implementation Skill

Implement a new feature following the complete workflow from planning to installation.

## Arguments

The skill accepts one of the following:
```
/feature #123          # GitHub issue number
/feature ?456          # GitHub discussion number
/feature Add dark mode # Free-form description
```

If no argument is provided, ask the user what feature they want to implement.

## Workflow

### 1. Understand the Feature Request

The repository is called `matedroid` and it is owned by `vide`.

**If an issue number is provided** (e.g., `#123` or just `123`):
- Use `mcp__github__issue_read` to get the issue details
- Note the language the issue was written in for future comments
- Read any existing comments for context

**If a discussion number is provided** (e.g., `?456`):
- Use `gh api repos/vide/matedroid/discussions/456` to fetch discussion details
- Note the language for future comments

**If a description is provided**:
- Use the description as the feature specification
- Ask clarifying questions if needed using AskUserQuestion

### 2. Explore and Plan

New features require careful planning. Use `EnterPlanMode` to:

1. **Explore the codebase** thoroughly:
   - Search for related existing code using Glob and Grep
   - Read relevant source files to understand current patterns
   - Identify files that will need modification
   - Look for similar features to follow existing patterns

2. **Design the implementation**:
   - List all files to be created or modified
   - Outline the approach step by step
   - Consider edge cases and error handling
   - Identify any new dependencies needed

3. **Write the plan** to the plan file for user approval

4. **Post the plan to the issue** (if an issue was specified):
   - Add a comment in the issue's original language
   - Include a summary of the planned approach
   - List the files to be modified
   - Ask for feedback before proceeding

5. **Exit plan mode** with `ExitPlanMode` when ready to implement

### 3. Create a Branch

Create a new branch for the feature:
```bash
git checkout -b feature/issue-<number>-<short-description>
# or for discussions:
git checkout -b feature/discussion-<number>-<short-description>
# or for descriptions:
git checkout -b feature/<short-description>
```

Examples:
- `feature/issue-42-dark-mode`
- `feature/discussion-15-trip-planner`
- `feature/battery-notifications`

### 4. Implement the Feature

Follow the approved plan to implement the feature:

- Follow existing code patterns and conventions
- Add comments only where the logic isn't self-evident
- All code comments MUST be in English
- Create new files as needed following project structure
- Update existing files minimally - don't refactor unrelated code

**Localization**: If adding user-visible strings:
- Add to all 4 locale files: `res/values/strings.xml`, `res/values-it/strings.xml`, `res/values-es/strings.xml`, `res/values-ca/strings.xml`
- Use snake_case for string names
- Never hardcode user-visible text

At the end, ALWAYS update `CHANGELOG.md` with the new feature under `[Unreleased]`.

### 5. Run Tests

Run the test suite to verify the feature doesn't break anything:
```bash
./gradlew testDebugUnitTest
```

Also run lint to catch common issues:
```bash
./gradlew lintDebug
```

Fix any failures before proceeding.

### 6. Commit and Push

Commit with a semantic commit message:
```bash
git add -A
git commit -m "feat: <description of feature> (#<issue-number>)"
# or without issue:
git commit -m "feat: <description of feature>"
git push -u origin <branch-name>
```

### 7. Install on Device

Build and install the debug APK on the connected device:
```bash
./gradlew installDebug
adb shell am start -n com.matedroid/.MainActivity
```

Let the user test the feature on their device.

### 8. Create Pull Request

Use `mcp__github__create_pull_request` to create a PR:
- **Title**: Same as the commit message (e.g., `feat: add dark mode (#42)`)
- **Body**: Must be in English and include:
  - Summary of the feature
  - Reference to the issue/discussion if applicable
  - Implementation details
  - Test plan

Example PR body:
```markdown
## Summary
Brief description of the new feature.

Closes #42

## Implementation
- What was added
- Key design decisions
- Any new dependencies

## Test Plan
- [ ] Manual testing steps
- [ ] Unit tests pass
- [ ] Tested on device

🤖 Generated with [Claude Code](https://claude.com/claude-code)
```

### 9. Update Issue/Discussion

If an issue or discussion was specified:
- Add a comment (in the original language) with:
  - Link to the PR
  - Summary of what was implemented
  - Any deviations from the original plan
  - Request for testing/feedback

### 10. Wait for Feedback

**Do NOT merge automatically.** Unlike bug fixes, features need:
- User testing and approval
- Possible iteration based on feedback
- CI to pass

Inform the user that the PR is ready for review and the app is installed on their device for testing.

## Language Rules

| Context | Language |
|---------|----------|
| Issue/discussion comments | Same language as the original |
| Plan posted to issue | Same language as the original |
| Code comments | English |
| Commit messages | English |
| PR title and body | English |

## Error Handling

- If requirements are unclear, use AskUserQuestion to clarify before planning
- If the feature conflicts with existing functionality, discuss in the issue before proceeding
- If tests fail, fix them before creating the PR
- If the device is not connected, skip the install step and notify the user
