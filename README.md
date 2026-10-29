# Sentinel

A GitOps-managed, fully observable reference platform for a Java microservice on Kubernetes. Sentinel pairs a Spring Boot order-management API with the infrastructure automation, delivery pipeline, and observability stack needed to deploy, operate, and diagnose it: metrics, logs, and distributed traces, all correlated in Grafana.

The workload runs on a small K3s cluster. The observability backends (Prometheus, Loki, Tempo, Grafana) run **outside the cluster** on a separate host, so telemetry stays available and queryable even when the cluster is down.

## Overview

The repository contains everything required to go from bare VMs to a running, monitored service:

- **`target-api`**: a Spring Boot order-management API (users, products, orders) backed by Oracle, used as the workload under observation.
- **Infrastructure as code**: Ansible provisions a two-node K3s cluster.
- **GitOps delivery**: ArgoCD reconciles the cluster against this repository using an app-of-apps pattern.
- **In-cluster telemetry collection**: kube-prometheus-stack (Prometheus only) scrapes metrics, and Grafana Alloy collects logs and traces and forwards them to an OpenTelemetry Collector gateway.
- **External observability stack**: a Docker Compose stack runs Prometheus, Loki, Tempo, an OpenTelemetry Collector, and Grafana, with cross-linking between the three signals.
- **Load testing**: k6 scenarios validate behavior and performance thresholds under load.

## Key Features

- **Workload**: CRUD and transactional order flows with a database dependency, so metrics, logs, and traces show meaningful behavior. OpenAPI/Swagger UI included.
- **Declarative cluster provisioning**: one Ansible playbook installs K3s on the control plane, joins the worker, and writes a local kubeconfig.
- **GitOps deployment**: ArgoCD with automated sync and self-heal; a single `root` application bootstraps every in-cluster component.
- **Metrics**: Micrometer + Prometheus via a `ServiceMonitor`, remote-written (with exemplars) to the external Prometheus, plus a `PrometheusRule` alert for API availability.
- **Centralized structured logs**: ECS-formatted JSON logs collected from every node by Alloy, sent over OTLP to the OTel Collector, and stored in Loki.
- **Distributed tracing**: OpenTelemetry Java agent exports traces through Alloy and the OTel Collector to Tempo, so logs and traces share one ingestion path.
- **Signal correlation**: jump from a log line to its trace, from a trace to its logs, and from a latency exemplar on a metric to the trace behind it.
- **Autoscaling and health management**: CPU-based HPA plus startup, liveness, and readiness probes backed by Spring Actuator.
- **CI/CD**: GitHub Actions runs integration tests against a real Oracle container, lints and renders both Helm charts, validates ArgoCD manifests, and publishes versioned images to GHCR on tagged releases.
- **Performance testing**: k6 smoke, ramping-load, and constant-arrival-rate throughput tests with pass/fail thresholds.

## Tech Stack

| Layer | Technologies |
|---|---|
| Application | Java 21, Spring Boot 4.1, Spring Web MVC, Spring Data JPA, Bean Validation, springdoc-openapi |
| Database | Oracle Database Free 23, Flyway |
| Containers | Docker, Docker Compose, GitHub Container Registry |
| Orchestration | K3s, Traefik (ingress), Helm |
| Provisioning | Ansible |
| GitOps / CI | ArgoCD, GitHub Actions |
| Metrics | Micrometer, Prometheus (kube-prometheus-stack in cluster, standalone Prometheus off-cluster), Prometheus Operator CRDs |
| Logs | Grafana Alloy, Loki |
| Traces | OpenTelemetry Java agent, Alloy (OTLP), OpenTelemetry Collector, Grafana Tempo |
| Visualization | Grafana |
| Private networking | Tailscale (cluster to monitoring host) |
| Load testing | k6, Python (data seeding) |

## Architecture

![Sentinel architecture](docs/images/architecture.svg)

**Delivery flow.** A Git tag (`vX.Y.Z`) triggers CI, which runs tests, validates the Helm charts, and pushes the image to GHCR. The new version is then promoted by updating the image tag in the chart; ArgoCD detects the change in Git and rolls it out to the cluster.

**Bootstrap.** `cluster/argocd/root.yaml` is an app-of-apps entry point. It points at `cluster/argocd/applications/`, where each file declares one in-cluster workload: `sentinel`, `oracle`, `monitoring`, and `alloy`. Adding a component to the cluster is a single new manifest in Git.

**Two halves.** The cluster runs the workload and the collection agents. The monitoring host runs the collector gateway, storage, and visualization. They communicate over a private Tailscale network, and exposed ports are bound only to the monitoring host's Tailscale address (Grafana is bound to localhost).

**Ingestion model.** Metrics follow the Prometheus-native path (Prometheus to Prometheus via remote-write). Logs and traces both go through the OTel Collector, which acts as a single gateway in front of Loki and Tempo, the same shape as sending telemetry to a vendor.

**Telemetry paths.**

| Signal | Path |
|---|---|
| Metrics | Sentinel exposes `/actuator/prometheus`. A `ServiceMonitor` tells the in-cluster Prometheus to scrape it every 15s. Prometheus remote-writes everything, exemplars included, to the external Prometheus (`/api/v1/write`). |
| Logs | Pods write ECS JSON to stdout. Alloy runs as a DaemonSet, tails `/var/log/pods` on each node, maps Kubernetes metadata (namespace, pod, container) to OTel resource attributes, and sends logs over OTLP gRPC to the OTel Collector. The collector batches them and forwards to Loki's OTLP endpoint. |
| Traces | The OTel Java agent sends OTLP/HTTP to Alloy, which batches and forwards over gRPC to the OTel Collector. The collector applies a memory limiter and batching, then forwards to Tempo. |

**Correlation.** Logs carry a `trace_id` field. Grafana's Loki datasource extracts it with a derived field to link directly to the trace in Tempo; Tempo is configured to query Loki for the logs belonging to a trace. Exemplar storage is enabled on both Prometheus instances and the API publishes trace-linked exemplars, connecting latency histograms to individual traces.

## Engineering Decisions

**GitOps with ArgoCD and an app-of-apps root.**
Git is the single source of truth for cluster state. Drift is automatically corrected (`selfHeal`), changes are auditable through commit history, and rebuilding the cluster is a matter of installing ArgoCD and applying one manifest. Pruning is deliberately disabled for the stateful database and the Alloy agent (Oracle, Alloy) so a bad commit cannot delete a PVC-backed workload or the telemetry pipeline, while the stateless API and the monitoring stack are allowed to prune.

**Observability backends are decoupled from the workload cluster.**
The cluster runs only lightweight collectors (Prometheus, Alloy) that ship telemetry to a separate monitoring host, the same shape as sending data to a managed observability vendor. This leaves the workload cluster's full capacity available for load and failure testing, and keeps telemetry queryable while the cluster is degraded or being rebuilt. Services are bound to a Tailscale address instead of being exposed publicly.

**Hand-written Helm charts for the application and database, upstream charts for everything else.**
The API and database are specific to this project, so their charts are owned in-repo. Platform components (kube-prometheus-stack, Alloy) use upstream charts with pinned versions and thin values files, which keeps upgrades a one-line change and reduces maintenance surface.

**In-cluster Prometheus is a collector, not a store.**
kube-prometheus-stack is deployed with Grafana disabled and remote-writes to the external Prometheus. It still provides the Prometheus Operator CRDs (`ServiceMonitor`, `PrometheusRule`), so scrape targets and alerts stay declarative and live next to the application chart. The external Prometheus runs with the remote-write receiver and exemplar storage enabled.

**ArgoCD ignores `spec.replicas` on the API Deployment.**
With the HPA enabled, the autoscaler owns replica count. Telling ArgoCD to ignore that field (and omitting `replicas` from the template when autoscaling is on) prevents the two controllers from fighting each other.

**Per-node agent plus a gateway collector for logs and traces.**
Alloy runs on every node and handles log collection and acts as the OTLP entry point for traces, so applications only need to know a single stable endpoint. Both signals then leave the cluster through one OTLP connection to the OTel Collector on the monitoring host, which applies a memory limiter and batching and routes logs to Loki and traces to Tempo. Redaction, sampling, enrichment, or swapping a backend becomes a change in one config file, without touching applications or the cluster. Metrics stay on the Prometheus-native remote-write path, since `ServiceMonitor`, `PrometheusRule`, and Prometheus remote-write are the standard for metrics.

**Structured logs with trace IDs over free-text logging.**
ECS-formatted JSON logs make fields queryable in Loki (`| json | trace_id="..."`) and make log-to-trace correlation reliable rather than regex-driven on arbitrary text.

**OpenTelemetry agent injected through `JAVA_TOOL_OPTIONS`.**
The agent is baked into the image but activated by the Helm chart. Tracing can be toggled per environment via `opentelemetry.enabled` without rebuilding, and the same image runs locally with no telemetry overhead. Exporters, sampler, and sampling ratio are all values-driven.

**Loki in monolithic mode with filesystem storage; Tempo with local storage.**
For a small setup, single-binary deployments avoid the operational cost of microservice-mode Loki and object storage. Retention is bounded (15 days for metrics, 7 days for logs, 24 hours for traces) to keep disk usage predictable. The Loki schema config uses TSDB with the v13 schema so a later move to object storage doesn't require a re-index.

**Startup probe tuned to measured boot time, separate liveness and readiness.**
The JVM plus Oracle connectivity takes a couple of minutes to come up on modest hardware. The startup probe waits 120s before its first check, then polls every 10s with a ceiling of about 7 minutes. This avoids a string of failed probes during a normal boot, still catches a genuinely stuck pod quickly, and stops liveness from killing a slow-starting pod. Readiness separately controls whether the pod receives traffic.

**Dashboards and datasources as code, alerts as Kubernetes resources.**
Grafana datasources (with the correlation settings) and the API dashboard are provisioned from files in `monitoring/grafana/`, so the monitoring stack comes up fully configured. Alerting is a `PrometheusRule` resource versioned alongside the application chart.

**Secrets kept out of Git.**
Database credentials are referenced through a Kubernetes Secret (`oracle-credentials`) created out-of-band, and the monitoring stack reads the Grafana admin password from an untracked `.env` file. Charts and manifests only contain secret names and keys.

**Load tests with explicit thresholds.**
k6 scripts encode expectations as thresholds (error rate under 1%, p95 latency under 500ms, zero dropped iterations for throughput runs), so a run produces a pass/fail result instead of a graph to interpret. The throughput test uses a constant-arrival-rate executor to measure capacity at a fixed request rate independent of response time, avoiding the coordinated-omission problem of closed-model tests.

**CI mirrors production dependencies.**
Integration tests run against a real Oracle Free container rather than an in-memory substitute, so dialect-specific SQL and Flyway migrations are exercised exactly as they run in the cluster. Helm charts are linted and rendered, and ArgoCD manifests are parsed, on every pull request.

## Getting Started

### Prerequisites

- Docker and Docker Compose (local run, and the monitoring host)
- Java 21 (optional, for running the API outside Docker)
- For the cluster setup: two Ubuntu/Debian hosts reachable over SSH, Ansible, `kubectl`, and Helm
- For the monitoring stack: a separate host reachable from the cluster nodes (this repo uses Tailscale)
- k6 and Python 3 (for load testing)

### Option 1: Run locally with Docker Compose

```bash
cd local
cp .env.example .env        # then set ORACLE_PASSWORD and DB_PASSWORD
docker compose up --build
```

| Service | URL |
|---|---|
| API | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Prometheus | http://localhost:9090 |
| Grafana | http://localhost:3000 |

Quick check:

```bash
curl http://localhost:8080/api/ping
```

> The local stack includes metrics and dashboards only. Log aggregation and tracing require the Kubernetes deployment and the monitoring stack below.

### Option 2: Deploy to Kubernetes (K3s + ArgoCD)

#### A. Start the monitoring stack

Run this on the monitoring host first, so the cluster has somewhere to send telemetry.

```bash
cd monitoring
cp .env.example .env        # then set GRAFANA_ADMIN_PASSWORD
docker compose up -d
```

`monitoring/compose.yaml` binds Prometheus (9090) and the OTel Collector (4317) to the host's Tailscale address, and Grafana to `127.0.0.1:3000`. Loki and Tempo receive data only from the collector over the Compose network. Replace `100.90.165.108` with your own monitoring host address in:

- `monitoring/compose.yaml` (port bindings)
- `cluster/observability/alloy/values.yaml` (OTLP gateway endpoint)
- `cluster/observability/prometheus/values.yaml` (remote-write URL)

Reach Grafana through an SSH tunnel or a local browser on the monitoring host:

```bash
ssh -L 3000:127.0.0.1:3000 <monitoring-host>
# then open http://localhost:3000
```

| Service | Exposed on | Retention |
|---|---|---|
| Prometheus | Tailscale IP :9090 (remote-write receiver) | 15 days |
| OTel Collector | Tailscale IP :4317 (OTLP gRPC gateway for logs and traces) | n/a |
| Loki | internal to Compose network (fed by the collector) | 7 days |
| Tempo | internal to Compose network (fed by the collector) | 24 hours |
| Grafana | 127.0.0.1:3000 | n/a |

#### B. Provision the cluster

Update `ansible/inventory/hosts.yaml` with your node IPs, SSH user, and key, then:

```bash
cd ansible
ansible-playbook playbooks/setup.yaml
export KUBECONFIG=~/.kube/sentinel-lab.yaml
kubectl get nodes
```

The cluster nodes must be able to reach the monitoring host (for example, by joining the same Tailscale network).

#### C. Label the workload node

```bash
kubectl label node worker node-role=workload
```

#### D. Install ArgoCD

```bash
helm repo add argo https://argoproj.github.io/argo-helm
helm install argocd argo/argo-cd -n argocd --create-namespace -f cluster/argocd/values.yaml
```

#### E. Create the database credentials secret

```bash
kubectl create namespace sentinel
kubectl create secret generic oracle-credentials -n sentinel \
  --from-literal=oracle-password='<admin-password>' \
  --from-literal=app-user-password='<app-password>'
```

#### F. Bootstrap the platform

```bash
kubectl apply -f cluster/argocd/root.yaml
```

ArgoCD will create and sync the `oracle`, `sentinel`, `monitoring`, and `alloy` applications. Watch progress with:

```bash
kubectl get applications -n argocd
```

#### G. Access the API

Add the ingress host to your machine's hosts file, pointing at a cluster node IP:

```
10.0.0.201  sentinel.lab.internal
```

```bash
curl http://sentinel.lab.internal/api/ping
```

Then open Grafana on the monitoring host (see step A) to explore metrics, logs, and traces.

### Load testing

```bash
cd tests/load

# Seed 10,000 products and write their IDs to product_ids.json
BASE_URL=http://sentinel.lab.internal python3 seed_products.py

# Smoke test
k6 run -e BASE_URL=http://sentinel.lab.internal smoke.js

# Ramping read load (5 → 20 virtual users)
k6 run -e BASE_URL=http://sentinel.lab.internal product-read-load-test.js

# Fixed-rate throughput test (default 25 req/s; override with RATE)
k6 run -e BASE_URL=http://<node-ip> -e RATE=50 product-read-throughput-test.js
```

### Releasing a new version

```bash
git tag v0.1.9
git push origin v0.1.9
```

CI runs the tests, validates the charts, and publishes `ghcr.io/phamnamson/sentinel:0.1.9`. Update `image.tag` in `cluster/apps/sentinel/values.yaml` and `appVersion` in `cluster/apps/sentinel/Chart.yaml`, then commit; ArgoCD rolls out the new version.

## Repository Layout

```
ansible/                      K3s provisioning (common, k3s_server, k3s_agent roles)
app/target-api/               Spring Boot application and Dockerfile
cluster/
  argocd/                     ArgoCD install values, root app, and application manifests
  apps/                       Helm charts for the API (sentinel) and Oracle
  observability/              Values for in-cluster Prometheus and Alloy
local/                        Local Docker Compose stack (API, Oracle, Prometheus, Grafana)
monitoring/                   External observability stack (Prometheus, Loki, Tempo, OTel Collector, Grafana)
tests/load/                   k6 scripts and data seeding
.github/workflows/            CI pipeline
```