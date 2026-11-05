#!/usr/bin/env bash
# One-time creation of the Azure resources used by the pipeline.
# Run it in Azure Cloud Shell (Bash) - az CLI is already logged in there.
#
#   bash azure-setup.sh
#
# It prints the values to store in GitHub (Settings > Secrets and variables > Actions).
set -euo pipefail

LOCATION="${LOCATION:-centralindia}"
RESOURCE_GROUP="${RESOURCE_GROUP:-rg-todo-cicd}"
AKS_CLUSTER="${AKS_CLUSTER:-aks-todo}"
# ACR names must be globally unique, lowercase letters/digits only
ACR_NAME="${ACR_NAME:-todoacr$RANDOM$RANDOM}"
NODE_SIZE="${NODE_SIZE:-Standard_B2s}"

echo ">> Registering resource providers (first use of a new subscription)"
az provider register --namespace Microsoft.ContainerService --wait
az provider register --namespace Microsoft.ContainerRegistry --wait
az provider register --namespace Microsoft.Network --wait
az provider register --namespace Microsoft.OperationalInsights --wait

echo ">> Resource group"
az group create -n "$RESOURCE_GROUP" -l "$LOCATION" -o table

echo ">> Azure Container Registry (Basic tier)"
az acr create -g "$RESOURCE_GROUP" -n "$ACR_NAME" --sku Basic -o table

echo ">> AKS cluster: 1 node, Free tier, allowed to pull from the ACR"
az aks create -g "$RESOURCE_GROUP" -n "$AKS_CLUSTER" \
  --tier free \
  --node-count 1 \
  --node-vm-size "$NODE_SIZE" \
  --attach-acr "$ACR_NAME" \
  --generate-ssh-keys \
  -o table

echo ">> Service principal used by GitHub Actions (Contributor on this RG only)"
SUB_ID=$(az account show --query id -o tsv)
SP_JSON=$(az ad sp create-for-rbac \
  --name "sp-todo-github-actions" \
  --role Contributor \
  --scopes "/subscriptions/$SUB_ID/resourceGroups/$RESOURCE_GROUP" \
  --json-auth)

cat <<EOF

==================================================================
 Add these in GitHub -> repo -> Settings -> Secrets and variables -> Actions

 Variables tab:
   RESOURCE_GROUP = $RESOURCE_GROUP
   AKS_CLUSTER    = $AKS_CLUSTER
   ACR_NAME       = $ACR_NAME

 Secrets tab:
   AZURE_CREDENTIALS = (the JSON below, including the braces)
==================================================================
$SP_JSON
EOF
