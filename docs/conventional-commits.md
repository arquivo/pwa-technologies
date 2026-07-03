# Conventional Commits at Arquivo.pt

> Status: **Proposal** — to be discussed at team meeting.

Arquivo.pt adopts [Conventional Commits](https://www.conventionalcommits.org/) across all repositories to bring consistency, enable automated changelogs, and make commit history machine-readable.

## Table of Contents

- [Commit message format](#commit-message-format)
- [Commit types](#commit-types)
- [Breaking changes](#breaking-changes)
- [Examples](#examples)
- [Enforcement](#enforcement)
- [Automated releases](#automated-releases)
- [Adopting in a new repository](#adopting-in-a-new-repository)

---

## Commit message format

```
<type>[optional scope]: <subject>

[optional body]

[optional footer(s)]
```

**Rules:**

- First line (header) should be under **110 characters**
- `type` is required and must be one of the [allowed types](#commit-types)
- `subject` is a short imperative description — no capital letter required, no trailing dot required
- `body` and `footer` are optional; no line-length limit enforced
- `scope` is optional and currently unstandardised across repos

---

## Commit types

| Type | Use for |
|---|---|
| `feat` | New feature or behaviour change |
| `fix` | Bug fix or security vulnerability mitigation |
| `perf` | Performance improvement |
| `revert` | Undoing a previous commit |
| `docs` | Documentation only (README, docstrings, comments) |
| `test` | Adding or fixing tests |
| `ci` | CI/CD pipeline changes |
| `refactor` | Code reorganisation with no behaviour change |
| `style` | Code style (formatting, whitespace) |
| `chore` | Maintenance tasks, dependency updates |

When multiple types apply, use the highest-priority one and explain the rest in the body.

---

## Breaking changes

Append `!` after the type, or add a `BREAKING CHANGE:` footer:

```
feat!: remove legacy search API

BREAKING CHANGE: The /api/v1/search endpoint has been removed. Use /api/v2/search instead.
```

Both forms are equivalent and will trigger a **major** version bump in automated releases.

---

## Examples

```
feat(search): add date-range filter to full-text search

fix: correct URL encoding for special characters in query string

docs: update README with Docker Compose setup instructions

ci: add commitlint workflow to validate PR commit messages

chore: upgrade express from 4.18 to 4.19

feat!: drop support for Node.js 16

BREAKING CHANGE: Node.js 18 or later is now required.
```

---

## Enforcement

Commit messages are validated automatically on every pull request via a GitHub Actions workflow shared across all Arquivo.pt repositories.

The shared workflows live in [arquivo/.github](https://github.com/arquivo/.github):

- **`commitlint.yml`** — runs on `pull_request`, validates every commit in the PR
- **`semantic-release.yml`** — runs on push to `master`, automates versioning and releases

### Configuration

The shared commitlint config (`commitlint.config.mjs`) enforces the types above with a relaxed style:

- 110-character header limit
- No body/footer line-length limit
- No subject-case enforcement
- No trailing-dot enforcement

Repositories can override this by shipping their own `commitlint.config.mjs` — the workflow uses the local file when present.

---

## Automated releases

When using the `semantic-release.yml` shared workflow, releases are created automatically on every push to `master` based on commit history:

| Commits since last release | Version bump |
|---|---|
| `feat!:` or `BREAKING CHANGE:` | **major** — `1.0.0 → 2.0.0` |
| `feat:` | **minor** — `1.0.0 → 1.1.0` |
| `fix:` / `perf:` / `revert:` | **patch** — `1.0.0 → 1.0.1` |
| `docs:`, `ci:`, `chore:`, … | no release |

Each release automatically:

1. Determines the next version from commit history
2. Generates and updates `CHANGELOG.md`
3. Bumps the version in `package.json` (if present)
4. Creates a GitHub Release with release notes

---

## Adopting in a new repository

Add two files to the repository:

**`.github/workflows/commitlint.yml`**

```yaml
name: Lint Commit Messages

on:
  pull_request:

jobs:
  commitlint:
    uses: arquivo/.github/.github/workflows/commitlint.yml@main
```

**`.github/workflows/semantic-release.yml`** *(optional — only for repos that publish releases)*

```yaml
name: Semantic Release

on:
  push:
    branches:
      - master

jobs:
  release:
    uses: arquivo/.github/.github/workflows/semantic-release.yml@main
    secrets: inherit
```

And a **`.releaserc.js`** at the repo root to configure release behaviour (see [arquivo-webapp-eros](https://github.com/arquivo/arquivo-webapp-eros/blob/ci/conventional-commits/.releaserc.js) as a reference).

---

## References

- [Conventional Commits specification](https://www.conventionalcommits.org/)
- [OEP-0051 — Open edX Conventional Commits](https://docs.openedx.org/projects/openedx-proposals/en/latest/best-practices/oep-0051-bp-conventional-commits.html)
- [arquivo/.github — shared workflows](https://github.com/arquivo/.github)
- [arquivo-webapp-eros#125 — pilot PR](https://github.com/arquivo/arquivo-webapp-eros/pull/125)
- [commitlint docs](https://commitlint.js.org/)
- [semantic-release docs](https://semantic-release.gitbook.io/)
