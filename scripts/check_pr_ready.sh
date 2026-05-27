#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT_DIR"

fail() {
  echo "[ERROR] $*" >&2
  exit 1
}

branch="$(git rev-parse --abbrev-ref HEAD)"
[ "$branch" != "HEAD" ] || fail "Detached HEAD detected. Checkout a branch before creating a PR."

if ! git remote get-url origin >/dev/null 2>&1; then
  fail "Missing origin remote. Add one: git remote add origin <repo-url>"
fi

if ! git ls-remote --exit-code --heads origin >/dev/null 2>&1; then
  fail "Cannot query origin heads. Check network/auth for origin remote."
fi

if ! git show-ref --verify --quiet refs/remotes/origin/main && \
   ! git show-ref --verify --quiet refs/remotes/origin/master; then
  echo "[WARN] origin/main and origin/master are both missing locally. Running fetch..."
  git fetch origin --prune
fi

if ! git show-ref --verify --quiet refs/remotes/origin/main && \
   ! git show-ref --verify --quiet refs/remotes/origin/master; then
  fail "No base branch found on origin (main/master). PR base is invalid."
fi

if git status --porcelain | grep -q .; then
  fail "Working tree is not clean. Commit or stash changes first."
fi

if git ls-files | rg -N '\.(glb|jar|png|jpg|jpeg|gif|webp|pdf|so|a|o|class)$' >/dev/null; then
  fail "Tracked binary file extensions detected. Remove binaries before PR."
fi

echo "[OK] PR preflight passed for branch '$branch'."
