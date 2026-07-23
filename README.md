# MonIT

Enterprise server health monitoring, built as a client/server pair. The client
("agent") runs on each server you want to watch and reports back to a central
server that aggregates everything and shows it on a simple dashboard.

# Authors/Contributors

- Joshua Quinn Stenger (JQS)
## How it works

- Each agent collects host metrics (CPU, memory, disk, network, uptime) and
  runs whatever checks you've configured for it (process running, port open,
  HTTP health endpoint, disk threshold).
- On first boot an agent registers itself with the server using a shared
  bootstrap secret and gets back an API key, which it stores locally and uses
  for every push after that.
- Every 30 seconds (configurable) the agent POSTs a report to the server,
  either over plain HTTP with its API key, or over mTLS if you've turned that
  on (see below).
- The server stores everything in TimescaleDB, checks every 15 seconds
  whether any client has gone offline or crossed a warning threshold, and
  emails the relevant recipients when a client's status actually changes
  (not on every tick).
- The dashboard is plain server-rendered pages (Thymeleaf) - an overview
  grid, a per-client detail page with a small CPU history chart and its own
  alert recipient list, and a page to manage the global alert recipient list.

## Project layout

```
common/   shared request/response classes used by both sides
agent/    the client that runs on a monitored server
server/   the aggregator + dashboard
```

Everything is a single Maven multi-module build.

## The database is yours to run

MonIT does not stand up TimescaleDB for you and never will - it only ships
Flyway migrations that create its own tables in a database you already have
running. Point it at a Postgres instance with the TimescaleDB extension
available (self-hosted, a managed service, whatever you already operate) and
give the server connection details via `spring.datasource.*` (or the
`SPRING_DATASOURCE_URL` / `SPRING_DATASOURCE_USERNAME` / `SPRING_DATASOURCE_PASSWORD`
environment variables). Flyway creates the schema on first startup; nothing
else touches the database's lifecycle, users, or backups.

## Running it

There's no docker-compose - the agent and server are independent images and
you run each as its own container, wired to your own database.

```
docker build -t monit-server -f server/Dockerfile .
docker run -d --name monit-server \
  -p 8081:8081 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://<your-db-host>:5432/monit \
  -e SPRING_DATASOURCE_USERNAME=monit \
  -e SPRING_DATASOURCE_PASSWORD=<password> \
  -e MONIT_BOOTSTRAP_SECRET=<shared-secret> \
  monit-server

docker build -t monit-agent -f agent/Dockerfile .
docker run -d --name monit-agent \
  -e MONIT_AGENT_SERVER_BASE_URL=http://<your-server-host>:8081 \
  -e MONIT_AGENT_BOOTSTRAP_SECRET=<shared-secret> \
  -v /proc:/host/proc:ro \
  -v /sys:/host/sys:ro \
  -v /:/host/root:ro \
  monit-agent
```

Then open http://localhost:8081 (or wherever you exposed the server).

The agent needs to see the *real* host's `/proc`, `/sys`, and disk to report
true metrics - mount those read-only as shown above, otherwise it'll only see
whatever it's stuck inside of.

## Building without Docker

You need Java 17 and Maven.

```
mvn -N install
mvn -f common/pom.xml install -DskipTests
mvn -f agent/pom.xml package
mvn -f server/pom.xml package
```

## Configuring an agent

Checks are declared in `checks.yml` next to the agent - no code, just list
what you want watched:

```yaml
checks:
  - type: process
    name: nginx-running
    processName: nginx
  - type: port
    name: app-port-8080
    port: 8080
  - type: http
    name: api-health
    url: http://localhost:8080/health
    expectStatus: 200
  - type: disk
    name: data-volume
    path: /data
    warnPercent: 85
```

Agent connection settings (server URL, bootstrap secret, push interval) live
in the agent's `application.yml`.

## Alert recipients

There's a global recipient list, edited from the dashboard's alert settings
page, and each client detail page has its own recipient list scoped to that
host. Both get notified - the per-client list is additive, not a replacement.

## Metric retention

The server applies a TimescaleDB retention policy to the `metrics` and
`check_results` hypertables on every startup, based on `monit.server.retention-days`
(default 30). Change it in `application.yml` or via `MONIT_SERVER_RETENTION_DAYS`
and restart - the old policy is dropped and replaced with the new window.

## mTLS between agent and server

Off by default; API keys alone are enough to get going. To turn it on, the
server needs a certificate keystore and a truststore containing the CA (or
certs) you'll sign agent certs with:

```yaml
# server application.yml
monit:
  server:
    mtls:
      enabled: true
      port: 8443
      keystore: /path/to/server-keystore.p12
      keystore-password: <password>
      truststore: /path/to/server-truststore.p12
      truststore-password: <password>
```

This opens a second connector on the server (port 8443 by default) that
requires a client certificate, alongside the normal HTTP port. It doesn't
replace the API key check - both apply.

Each agent then needs its own client keystore and a truststore that trusts
the server's certificate:

```yaml
# agent application.yml
monit:
  agent:
    mtls:
      enabled: true
      keystore: /path/to/agent-keystore.p12
      keystore-password: <password>
      truststore: /path/to/agent-truststore.p12
      truststore-password: <password>
```

### Generating certs with OpenSSL

Everything here needs a PKCS12 keystore/truststore, OpenSSL can generate the keys and certs and package them into PKCS12 directly. A minimal self-signed CA plus one cert per side looks like this:

```bash
# 1. A CA to sign both certs
openssl req -x509 -newkey rsa:4096 -keyout ca-key.pem -out ca-cert.pem \
  -days 3650 -nodes -subj "/CN=MonIT Test CA"

# 2. Server key + cert, signed by the CA
openssl req -newkey rsa:2048 -keyout server-key.pem -out server-csr.pem \
  -nodes -subj "/CN=monit-server"
openssl x509 -req -in server-csr.pem -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out server-cert.pem -days 825

# 3. Agent key + cert, signed by the same CA
openssl req -newkey rsa:2048 -keyout agent-key.pem -out agent-csr.pem \
  -nodes -subj "/CN=agent-01"
openssl x509 -req -in agent-csr.pem -CA ca-cert.pem -CAkey ca-key.pem \
  -CAcreateserial -out agent-cert.pem -days 825

# 4. Package each side into a PKCS12 keystore
openssl pkcs12 -export -inkey server-key.pem -in server-cert.pem \
  -certfile ca-cert.pem -out server-keystore.p12 -name server -passout pass:changeit
openssl pkcs12 -export -inkey agent-key.pem -in agent-cert.pem \
  -certfile ca-cert.pem -out agent-keystore.p12 -name agent -passout pass:changeit

# 5. Truststores just need the CA cert, no key - both sides can share this file
openssl pkcs12 -export -nokeys -in ca-cert.pem \
  -out ca-truststore.p12 -passout pass:changeit
```

Point `monit.server.mtls.keystore` at `server-keystore.p12` and
`monit.server.mtls.truststore` at `ca-truststore.p12`; point
`monit.agent.mtls.keystore` at `agent-keystore.p12` and
`monit.agent.mtls.truststore` at the same `ca-truststore.p12`. For anything
beyond local testing, issue agent certs from your organization's real CA
instead of a throwaway one.

---

