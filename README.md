# Sentinel Lab

## 🌐 Overview
Lorem ipsum dolor sit amet, consectetuer adipiscing elit. Aenean commodo ligula eget dolor. Aenean massa. Cum sociis natoque penatibus et magnis dis parturient montes, nascetur ridiculus mus. Donec quam felis, ultricies nec, pellentesque eu, pretium quis, sem. Nulla consequat massa quis enim. Donec pede justo, fringilla vel, aliquet nec, vulputate eget, arcu. In enim justo, rhoncus ut, imperdiet a, venenatis vitae, justo. Nullam dictum felis eu pede mollis pretium. Integer tincidunt. Cras dapibus. Vivamus elementum semper nisi. Aenean vulputate eleifend tellus. Aenean leo ligula, porttitor eu, consequat vitae, eleifend ac, enim. Aliquam lorem ante, dapibus in, viverra quis, feugiat a,

```
Ansible  ->  K3s cluster  ->  Argo CD  ->  Target API + Oracle
(provision)  (control plane    (GitOps      (workload)
              + worker)         deploy)
```
 
- **Provision**: 
- **Deploy**: Helm charts and Argo CD Applications describe the workloads in Git.
- **Run**: a small e-commerce API with an Oracle database acts as the workload.

---

## 🛠️ Ansible (K3s Cluster Provisioning)

Automates the setup of a two-node Kubernetes cluster, taking fresh Debian/Ubuntu machines to a working K3s cluster with a single playbook. The cluster can be wiped and rebuilt the same way every time.

- **Stack**: Ansible, K3s, Debian/Ubuntu.
- **Cluster**: 1 control plane (`10.0.0.201`) and 1 worker (`10.0.0.202`).
- **Common role**: installs base packages on every node.
- **K3s server role**: installs a pinned K3s version on the control plane, waits for the API to be ready, and saves a kubeconfig locally at `~/.kube/sentinel-lab.yaml`.
- **K3s agent role**: reads the join token from the control plane, joins the worker to the cluster, and waits until the node is `Ready`.
- **Re-runnable**: installs are skipped when K3s is already present, and the join token is hidden from logs.

<!-- 📄 Details: [ansible/README.md](ansible/README.md) TODO  -->

---

## 🎯 Target API (Spring Boot)

A small e-commerce REST API (users, products, orders) that exists to be tested, monitored, and scaled. The business logic is deliberately simple. The point is to have a realistic workload to practice on.

- **Stack**: Java 21, Spring Boot, Oracle Database, Flyway.
- **Ops-ready**: Actuator health probes (liveness and readiness), Swagger UI, and a multi-stage Docker image.
- **Order creation**: checks and decrements stock in one transaction, which gives a write path with real database work to load test.

### Endpoints

| Resource | Endpoints |
| -------- | --------- |
| **Users** | `POST /api/user`, `GET /api/user/{id}`, `GET /api/user/{id}/orders` |
| **Products** | `POST /api/product`, `GET /api/product/{id}`, `GET /api/product/search`, `PATCH /api/product/{id}/stock` |
| **Orders** | `POST /api/order`, `GET /api/order/{id}`, `POST /api/order/{id}/cancel` |
| **Ops** | `GET /api/ping`, `GET /actuator/health` |

<!-- 📄 Details: [app/target-api/README.md](app/target-api/README.md) TODO -->

---

## 🚀 Deploy (Docker Compose + Helm + Argo CD)

Two ways to run the same stack (Spring API and Oracle Database): Docker Compose for quick local runs, and Helm charts deployed to the cluster with Argo CD.

- **Docker Compose**: starts Oracle and the API together. The API waits for the database healthcheck to pass, and data persists in a volume.
- **Helm charts**: one for Oracle (StatefulSet with a 5Gi persistent volume) and one for the API (Deployment with startup, liveness and readiness probes wired to Actuator).
- **Argo CD**: two Applications watch this repo and deploy both charts into the `sentinel` namespace.
- **Secrets**: database credentials live in a Kubernetes Secret (`oracle-credentials`) and in a local `.env` file for Compose. Only `.env.example` is committed.
- **Resource limits**: set on every workload, so scaling and load-test results mean something.

<!-- 📄 Details: [deploy/README.md](deploy/README.md) TODO -->

---

### Structure
```
ansible/
├─ inventory/         # Hosts and shared vars (k3s_version)
├─ playbooks/         # setup.yaml entry point
└─ roles/             # common, k3s_server, k3s_agent
app/
└─ target-api/        # Spring Boot API
   ├─ src/main/java/       # Controllers, services, repositories, entities
   ├─ src/main/resources/  # Config + Flyway migrations
   └─ Dockerfile           # Multi-stage build
deploy/
├─ compose/           # Docker Compose for local runs
├─ kubernetes/        # Helm charts (oracle, sentinel)
└─ argocd/            # Argo CD values and Applications
```
 
---