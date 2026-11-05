# To-Do List – End-to-End CI/CD on Azure (CIA 2)

A very simple Spring Boot to-do list web app, built, tested, containerised and deployed to
**Azure Kubernetes Service (AKS)** automatically by a **GitHub Actions** pipeline on every push.

```
 Developer ──git push──▶ GitHub repo ──triggers──▶ GitHub Actions
                                                     │
   ┌─────────────────────────────────────────────────┘
   ▼
 [1] Checkout ─▶ [2] Maven compile+package ─▶ [3] JUnit tests (13)
   ─▶ [4] docker build + tag (1.0.<run>-<sha>) ─▶ [5] push to Azure Container Registry
   ─▶ [6] Ansible playbook applies K8s manifests on AKS (rolling update)
   ─▶ [7] verify: rollout status, LoadBalancer IP, /actuator/health, /api/info == new version
                                                     │
 Browser ◀──── http://<public-ip>  (Azure Load Balancer ─▶ Service ─▶ 2 pods)
```

## Repository layout

| Path | Purpose |
|---|---|
| `pom.xml` | Maven build (Spring Boot 3.3, Java 17) |
| `src/main/java/com/example/todo/` | App: `TodoService` (logic), `TodoController` (REST API) |
| `src/main/resources/static/index.html` | Web UI |
| `src/test/java/...` | 7 unit tests + 6 API integration tests |
| `Dockerfile`, `.dockerignore` | Runtime image (JRE 17 alpine, non-root user) |
| `.github/workflows/ci-cd.yml` | CI/CD pipeline (3 jobs, 7 stages) |
| `k8s/` | Namespace, Deployment (2 replicas, probes, limits), LoadBalancer Service |
| `ansible/` | Inventory + `deploy.yml` playbook (deploy + smoke tests) |
| `infra/azure-setup.sh` | Creates RG, ACR, AKS, service principal (run in Azure Cloud Shell) |
| `infra/azure-cleanup.sh` | Deletes everything after evaluation |

## Run locally

```bash
mvn clean verify                 # compile + 13 tests + jar
java -jar target/todo-app.jar    # http://localhost:8080
docker build -t todo-app:local . && docker run -p 8080:8080 todo-app:local
```

## One-time Azure + GitHub setup

1. Azure portal → Cloud Shell (Bash) → upload `infra/azure-setup.sh` → `bash azure-setup.sh`
   (~10 min). It prints three variables and one secret.
2. GitHub repo → Settings → Secrets and variables → Actions:
   * Variables: `RESOURCE_GROUP`, `AKS_CLUSTER`, `ACR_NAME`
   * Secret: `AZURE_CREDENTIALS` (the JSON)
3. Push to `main` → Actions tab shows the pipeline running.

## Change-and-redeploy demo (for the viva)

1. Edit `src/main/resources/static/index.html`, e.g. change `<h1>My To-Do List</h1>`
   to `<h1>My To-Do List v2</h1>`.
2. `git commit -am "Change heading to v2" && git push`
3. GitHub → Actions: the run starts automatically; watch the 3 jobs go green.
4. Azure portal → Container registry → Repositories → `todo-app`: a new tag `1.0.<run>-<sha>`.
5. `kubectl get pods -n todo` – new pods replace old ones (rolling update, no downtime).
6. Refresh `http://<public-ip>` – new heading, and the footer shows the new version.

## Useful commands (Cloud Shell)

```bash
az aks get-credentials -g rg-todo-cicd -n aks-todo
kubectl get deploy,pods,svc -n todo -o wide
kubectl rollout history deployment/todo-app -n todo
az acr repository show-tags -n <ACR_NAME> --repository todo-app -o table
```
