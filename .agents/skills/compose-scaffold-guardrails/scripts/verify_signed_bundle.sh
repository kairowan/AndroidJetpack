#!/usr/bin/env bash
set -euo pipefail

if [[ "$#" != "1" ]]; then
    printf 'Usage: %s /absolute/path/to/signed-release.aab\n' "$0" >&2
    exit 64
fi

bundle_path="$1"
if [[ "$bundle_path" != /* || "$bundle_path" != *.aab || ! -f "$bundle_path" ]]; then
    printf 'FAILED: provide an existing absolute .aab path.\n' >&2
    exit 1
fi

jarsigner -verify -strict -certs "$bundle_path"
printf 'Signed Android App Bundle verification passed: %s\n' "$bundle_path"
