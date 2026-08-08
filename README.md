# OpenTelemetry Distributed Tracing

Distributed tracing laboratory based on independent Java microservices and OpenTelemetry.

The goal of this project is to demonstrate how a request can be traced end-to-end through a distributed architecture involving multiple microservices, synchronous communication, and asynchronous messaging.

## Goals

The project will progressively explore:

- Distributed tracing.
- OpenTelemetry.
- Trace and span context propagation.
- Synchronous communication between microservices.
- Asynchronous messaging.
- Automatic and manual instrumentation.
- OpenTelemetry Collector.
- Trace visualization and troubleshooting.
- Log and trace correlation.
- Metrics and observability.

## Planned Architecture

```text
Client
  |
  v
Order Service
  |
  +----> Inventory Service
  |
  +----> Payment Service
  |
  +----> Messaging
            |
            v
     Notification Service


Microservices
      |
      | OTLP
      v
OpenTelemetry Collector
      |
      v
Observability Backend
```

The architecture will be implemented incrementally throughout the project.

## Repository Approach

The project uses a monorepo to keep the complete distributed system in a single repository.

Each microservice will remain an independent application with its own:

- Maven project.
- Dependencies.
- Maven Wrapper.
- Configuration.
- Tests.
- Docker image.

There will be no Maven multi-module build or shared Maven parent at repository level.

The repository will progressively evolve towards:

```text
opentelemetry-distributed-tracing/
├── services/
│   ├── order-service/
│   ├── inventory-service/
│   ├── payment-service/
│   └── notification-service/
│
├── infrastructure/
│
├── docker-compose.yml
└── README.md
```

Directories and infrastructure components will only be added when required by their corresponding implementation.
