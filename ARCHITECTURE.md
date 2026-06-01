# StoreFlow — Architecture & Flow

## What is StoreFlow?

StoreFlow is a retail order management REST API. It handles the full lifecycle of a store order — from creation through processing, classification, and cancellation — with a full audit trail on every state change.

---

## Application Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT                               │
│              (Swagger UI / HTTP Consumer)                   │
└──────────────────────────┬──────────────────────────────────┘
                           │ HTTP
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   FILTER LAYER                              │
│              CorrelationIdFilter                            │
│         Assigns X-Correlation-ID to every request          │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                  CONTROLLER LAYER                           │
│                   OrderController                           │
│     Validates input · Delegates to service · Maps response  │
└──────────────────────────┬──────────────────────────────────┘
                           │
                           ▼
┌─────────────────────────────────────────────────────────────┐
│                   SERVICE LAYER                             │
│         OrderService              StoreService              │
│    Business logic · Caching    Store metadata · Caching     │
└────────────┬─────────────────────────────┬──────────────────┘
             │                             │
             ▼                             ▼
┌────────────────────────┐    ┌────────────────────────────┐
│   REPOSITORY LAYER     │    │       CACHE LAYER          │
│                        │    │                            │
│  OrderRepository       │    │  orders    — 10 min TTL   │
│  OrderAuditRepository  │    │  summary   — 30 sec TTL   │
│                        │    │  stores    — 1 hour TTL   │
└────────────┬───────────┘    └────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────────────────┐
│                      DATABASE                               │
│                      MySQL 8.0                              │
│                                                             │
│   orders              order_audit_history                   │
└─────────────────────────────────────────────────────────────┘
```

---

## Order Lifecycle

```
         POST /api/orders
               │
               ▼
         ┌─────────┐
         │ PENDING │  ◄── Order created, not yet processed
         └────┬────┘
              │
     ┌────────┴────────┐
     │                 │
     ▼                 ▼
┌─────────┐      ┌───────────┐
│PROCESSED│      │ CANCELLED │
└─────────┘      └───────────┘
     ▲
     │
POST /api/orders/{id}/process
     │
     └── Validates store
         Calculates total price
         Applies category discount
         Classifies order (STANDARD / LARGE / VIP)
         Records audit event
```

---

## Order Classification

```
  Final Price (after discount)
         │
         ├──── < €500   ──► STANDARD
         │
         ├──── €500–999 ──► LARGE
         │
         └──── ≥ €1000  ──► VIP
```

---

## Category Discounts

```
  FURNITURE   ──► 5% discount
  MATTRESSES  ──► 8% discount
  BEDDING     ──► 3% discount
  TEXTILES    ──► 2% discount
  LIGHTING    ──► no discount
```

---

## Request Flow (Happy Path)

```
  Client                Filter           Controller        Service           DB
    │                     │                  │                │               │
    │── POST /orders ─────►                  │                │               │
    │                     │── assign ──────► │                │               │
    │                     │   correlationId  │                │               │
    │                     │                  │── @Valid ─────►│               │
    │                     │                  │   validate     │               │
    │                     │                  │                │── save() ────►│
    │                     │                  │                │◄── Order ─────│
    │                     │                  │                │── audit() ───►│
    │◄── 201 Created ──────────────────────────────────────────               │
    │
    │── POST /orders/{id}/process ─────────────────────────────────────────►  │
    │                                                        │── findById() ──►│
    │                                                        │── enrich()      │
    │                                                        │── calculate()   │
    │                                                        │── classify()    │
    │                                                        │── save() ──────►│
    │                                                        │── audit() ─────►│
    │◄── 200 OK (PROCESSED, classification, finalPrice) ──────                │
```

---

## Audit Trail

Every state transition writes an immutable record to `order_audit_history`:

```
  GET /api/orders/{id}/audit

  [
    { from: null,      to: PENDING,   triggeredBy: API, note: "Order created" },
    { from: PENDING,   to: PROCESSED, triggeredBy: API, note: "classification=VIP finalPrice=1140.00" }
  ]
```

---

## Infrastructure

```
  docker-compose up --build
        │
        ├── mysql (port 3306)
        │     MySQL 8.0
        │     Database: storeflow_db
        │     Healthcheck before app starts
        │
        └── app (port 8080)
              Spring Boot
              Swagger UI: http://localhost:8080/swagger-ui/index.html
              Debug port: 5005
```

---

## API Summary

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/orders` | Create a new order |
| GET | `/api/orders` | Search with filters + pagination |
| GET | `/api/orders/{id}` | Get order by ID |
| PUT | `/api/orders/{id}` | Update a pending order |
| POST | `/api/orders/{id}/process` | Process an order |
| POST | `/api/orders/{id}/cancel` | Cancel an order |
| GET | `/api/orders/{id}/audit` | Full audit history |
| GET | `/api/orders/summary` | Revenue & classification stats |
