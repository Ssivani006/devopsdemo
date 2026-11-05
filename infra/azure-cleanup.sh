#!/usr/bin/env bash
# Deletes everything created by azure-setup.sh (stop paying for the cluster
# after the evaluation). Run in Azure Cloud Shell.
set -euo pipefail
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-todo-cicd}"

az group delete -n "$RESOURCE_GROUP" --yes --no-wait
SP_ID=$(az ad sp list --display-name sp-todo-github-actions --query "[0].appId" -o tsv)
[ -n "$SP_ID" ] && az ad sp delete --id "$SP_ID"
echo "Deletion started for $RESOURCE_GROUP (takes a few minutes)."
