#!/bin/bash

# https://docs.github.com/en/rest/apps/apps?apiVersion=2022-11-28#create-an-installation-access-token-for-an-app--parameters

# CODE_PERMISSION=${CODE_PERMISSION:-"null"}
# PR_PERMISSION=${PR_PERMISSION:-"null"}
CHECKS_PERMISSION=${CHECKS_PERMISSION:-"null"}
DEPLOYMENTS_PERMISSION=${DEPLOYMENTS_PERMISSION:-"null"}
ENVIRONMENTS_PERMISSION=${ENVIRONMENTS_PERMISSION:-"null"}
PACKAGES_PERMISSION=${PACKAGES_PERMISSION:-"null"}
ADMIN_PERMISSION=${ADMIN_PERMISSION:-"null"}
WORKFLOW_PERMISSION=${WORKFLOW_PERMISSION:-"null"}
ACTIONS_PERMISSION=${ACTIONS_PERMISSION:-"null"}

echo "::add-mask::$INSTALLATION_ID"
echo "::add-mask::$APP_PRIVATE_KEY"

read -r -d '' PERMISSIONS_JSON << EOF
{
    "checks": "$CHECKS_PERMISSION",
    "contents": "$CODE_PERMISSION",
    "deployments": "$DEPLOYMENTS_PERMISSION",
    "environments": "$ENVIRONMENTS_PERMISSION",
    "packages": "$PACKAGES_PERMISSION",
    "pull_requests": "$PR_PERMISSION",
    "administration": "$ADMIN_PERMISSION",
    "workflows": "$WORKFLOW_PERMISSION",
    "actions": "$ACTIONS_PERMISSION",
    "metadata": "read"
}
EOF

PERMISSIONS_JSON=${PERMISSIONS_JSON//[[:space:]]/} ## replace all whitespace with the empty string
# Filter out "null" (note the quotes) permissions we don't need - do we even need this bit?
FILTERED_PERMISSIONS=$(jq 'map_values(select(. != "null"))' <<<$PERMISSIONS_JSON)

REPOS_JSON=$(jq 'split(",")'<<<\""$REPO_NAMES\"")

read -r -d '' PAYLOAD << EOF
{
    "repositories": $REPOS_JSON,
    "permissions": $FILTERED_PERMISSIONS
}
EOF

echo -e "Will send this payload to Github:\n"
echo "$PAYLOAD"

# The JWT signing bit is borrowed from https://gist.github.com/carestad/bed9cb8140d28fe05e67e15f667d98ad

# Shared content to use as template
header='{
    "alg": "RS256",
    "typ": "JWT"
}'
payload_template='{}'

build_payload() {
        jq -c \
                --arg iat_str "$(date +%s)" \
                --arg app_id "${APP_ID}" \
        '
        ($iat_str | tonumber) as $iat
        | .iat = $iat
        | .exp = ($iat + 300)
        | .iss = ($app_id | tonumber)
        ' <<< "${payload_template}" | tr -d '\n'
}

b64enc() { openssl enc -base64 -A | tr '+/' '-_' | tr -d '='; }
json() { jq -c . | LC_CTYPE=C tr -d '\n'; }
rs256_sign() { openssl dgst -binary -sha256 -sign <(printf '%s\n' "$1"); }

sign() {
    local algo payload sig
    algo=${1:-RS256}; algo=${algo^^}
    payload=$(build_payload) || return
    signed_content="$(json <<<"$header" | b64enc).$(json <<<"$payload" | b64enc)"
    sig=$(printf %s "$signed_content" | rs256_sign "$APP_PRIVATE_KEY" | b64enc)
    printf '%s.%s\n' "${signed_content}" "${sig}"
}

GENERATED_JWT=$(sign)

GITHUB_RESPONSE=$(curl -s -L -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer ${GENERATED_JWT}" \
    -H "X-GitHub-Api-Version: 2022-11-28" \
    https://api.github.com/app/installations/${INSTALLATION_ID}/access_tokens \
    -d "$PAYLOAD")
    # -d "{\"repositories\":$REPOS_JSON,\"permissions\":{\"pull_requests\":\"$PR_PERMISSION\",\"contents\":\"$CODE_PERMISSION\", \"deployments\":\"$DEPLOYMENTS_PERMISSION\"}}")

if jq -e '.message' <<<"$GITHUB_RESPONSE" >/dev/null; then

    echo -e "Got a message from Github, likely failed to authenticate.\nGITHUB RESPONSE:\n"
    jq -r '.message' <<<"$GITHUB_RESPONSE"
    exit 1

else

    GITHUB_TOKEN=$(jq -r .token <<<"$GITHUB_RESPONSE")
    echo "::add-mask::$GITHUB_TOKEN"
    echo "github-token=$GITHUB_TOKEN" >> $GITHUB_OUTPUT

    echo "Got a token that will expire at: $(jq -r '.expires_at' <<<"$GITHUB_RESPONSE")"
    echo -e "Token has these permissions:\n$(jq -r '.permissions' <<<"$GITHUB_RESPONSE")"
    echo -e "With access to these repos:\n$(jq -r '.repositories[].full_name' <<<"$GITHUB_RESPONSE")"
fi