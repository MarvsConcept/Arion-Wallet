# ArionWallet

ArionWallet is a Spring Boot wallet backend that models common fintech workflows: customer onboarding, JWT authentication, wallet funding, peer-to-peer transfers, bank withdrawals, KYC review, role-based administration, transaction history, and double-entry ledger records.

The codebase is a modular monolith. Each business module owns its presentation, application, domain, and infrastructure layers, preserving clear boundaries while remaining easy to run and deploy as one service.

> **Status:** Active development. This learning/demo project is not production-ready. Read the [security notes](#security-notes) before exposing it to a network or using real credentials.

## Features

- Registration, BCrypt password hashing, and stateless JWT authentication
- `USER`, `ADMIN`, and `COMPLIANCE` role-based access control
- Automatic wallet and account-number creation
- Paystack or Flutterwave wallet funding and payouts
- Idempotent funding, transfer, and withdrawal requests
- Internal wallet-to-wallet transfers
- Saved/default bank accounts with provider-based name resolution
- Asynchronous withdrawals, wallet holds, and timeout handling
- Verified Paystack and Flutterwave webhooks
- KYC submission and compliance review
- Configurable fraud limits, audit logging, and administrative user controls
- Paginated transaction/withdrawal history and double-entry ledger records
- OpenAPI/Swagger documentation and Actuator

## Stack

| Area | Technology |
| --- | --- |
| Language/framework | Java 17, Spring Boot 3.5.8 |
| API | Spring Web MVC, Bean Validation |
| Security | Spring Security, JWT (`jjwt`), BCrypt |
| Persistence | Spring Data JPA, Hibernate, PostgreSQL |
| Documentation | Springdoc OpenAPI / Swagger UI |
| Build/testing | Maven Wrapper, JUnit 5, Mockito, Spring Security Test |
| Local infrastructure | Docker Compose, PostgreSQL, Adminer |

## Architecture

```text
src/main/java/com/marv/arionwallet
├── core
│   ├── config          # security, providers, HTTP client, OpenAPI
│   ├── dto             # shared API response envelope
│   ├── presentation    # health and global exception handling
│   └── security        # JWT creation and authentication filter
└── modules
    ├── admin           # user state and role administration
    ├── audit           # administrative audit trail
    ├── auth            # login, roles, admin bootstrapping
    ├── banking         # banks and beneficiary accounts
    ├── fraud           # transfer and withdrawal limits
    ├── funding         # wallet top-ups
    ├── kyc             # customer KYC and compliance review
    ├── ledger          # double-entry ledger
    ├── payments        # funding providers and webhooks
    ├── payout          # payout providers and webhooks
    ├── transaction     # transaction lifecycle and history
    ├── transfer        # wallet-to-wallet transfers
    ├── user            # registration and profiles
    ├── wallet          # balances and holds
    └── withdrawal      # withdrawal orchestration and jobs
```

Modules generally contain `presentation` (controllers/DTOs), `application` (use cases), `domain` (entities and repository interfaces), and `infrastructure` (JPA and external adapters).

## Prerequisites

- JDK 17
- Docker and Docker Compose (recommended for PostgreSQL)
- Maven 3.6+ only when not using the included wrapper
- Paystack or Flutterwave test credentials for provider-backed flows

## Quick start

### 1. Clone and enter the project

```bash
git clone <repository-url>
cd arionwallet
```

### 2. Configure secrets

The checked-in `application.yml` has development defaults. Prefer environment variables and never use its placeholder JWT secret or committed provider credentials outside local development:

```bash
export ARIONWALLET_JWT_SECRET="replace-with-a-long-random-secret"
export ARIONWALLET_BOOTSTRAP_ADMIN_EMAIL="admin@example.com"
export ARIONWALLET_PAYMENTS_PROVIDER="flutterwave"
export ARIONWALLET_PAYOUTS_PROVIDER="flutterwave"
export PAYSTACK_SECRET_KEY="your-paystack-test-secret"
export FLUTTERWAVE_SECRET_KEY="your-flutterwave-test-secret"
export FLUTTERWAVE_PUBLIC_KEY="your-flutterwave-test-public-key"
export FLUTTERWAVE_ENCRYPTION_KEY="your-flutterwave-encryption-key"
export FLUTTERWAVE_REDIRECT_URL="http://localhost:8080/payment/callback"
export FLUTTERWAVE_WEBHOOK_SECRET_HASH="your-webhook-secret-hash"
```

PowerShell uses `$env:NAME = "value"` instead of `export NAME="value"`.

### 3. Start PostgreSQL

```bash
docker compose up -d db
```

The Compose file exposes PostgreSQL on `localhost:5432` with database `arionwallet` and development username/password `arion`. Run `docker compose up -d` to also launch Adminer at [http://localhost:8888](http://localhost:8888).

### 4. Run the API

```bash
# macOS/Linux
./mvnw spring-boot:run

# Windows PowerShell
.\mvnw.cmd spring-boot:run
```

Then open:

- Health: [http://localhost:8080/api/v1/health](http://localhost:8080/api/v1/health)
- Swagger UI: [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
- OpenAPI JSON: [http://localhost:8080/v3/api-docs](http://localhost:8080/v3/api-docs)

## Configuration

| Environment variable | Default | Purpose |
| --- | --- | --- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5432/arionwallet` | PostgreSQL URL |
| `SPRING_DATASOURCE_USERNAME` | `arion` | Database username |
| `SPRING_DATASOURCE_PASSWORD` | `arion` | Database password |
| `ARIONWALLET_JWT_SECRET` | Development placeholder | JWT signing secret |
| `ARIONWALLET_JWT_EXPIRATION_SECONDS` | `8400000` | Token lifetime in seconds |
| `ARIONWALLET_BOOTSTRAP_ADMIN_EMAIL` | Development email | Grants `ADMIN` to a matching user at startup |
| `ARIONWALLET_PAYMENTS_PROVIDER` | `flutterwave` | `paystack` or `flutterwave` funding provider |
| `ARIONWALLET_PAYOUTS_PROVIDER` | `flutterwave` | `paystack` or `flutterwave` payout provider |
| `PAYSTACK_SECRET_KEY` | No safe default | Paystack API/signing secret |
| `FLUTTERWAVE_BASE_URL` | `https://api.flutterwave.com` | Provider base URL |
| `FLUTTERWAVE_SECRET_KEY` | No safe default | Provider API secret |
| `FLUTTERWAVE_PUBLIC_KEY` | No safe default | Provider public key |
| `FLUTTERWAVE_ENCRYPTION_KEY` | No safe default | Provider encryption key |
| `FLUTTERWAVE_REDIRECT_URL` | Empty | Checkout redirect destination |
| `FLUTTERWAVE_WEBHOOK_SECRET_HASH` | Empty | Webhook verification hash |

Provider names are case-insensitive. Hibernate currently uses `ddl-auto: update`; replace this with versioned migrations before production use.

## Authentication and roles

Health, registration, login, bank listing, webhooks, and API docs are public. All other endpoints require:

```http
Authorization: Bearer <jwt-token>
```

| Capability | Role |
| --- | --- |
| Grant/revoke roles | `ADMIN` |
| Review KYC | `ADMIN` or `COMPLIANCE` |
| Freeze/unfreeze users | `ADMIN` or `COMPLIANCE` |

The application seeds all roles at startup. If `ARIONWALLET_BOOTSTRAP_ADMIN_EMAIL` matches a registered user, it grants that user `ADMIN`. Locally, start the app, register that email, and restart once.

## API reference

Normal JSON responses use this envelope:

```json
{
  "success": true,
  "message": "Request successful",
  "data": {},
  "timestamp": "2026-01-01T12:00:00Z"
}
```

### Public

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/health` | Health check |
| `POST` | `/api/v1/users/register` | Register a customer |
| `POST` | `/api/v1/auth/login` | Receive a JWT |
| `GET` | `/api/v1/banks` | List provider-supported banks |
| `POST` | `/api/v1/webhooks/paystack` | Paystack funding/payout events |
| `POST` | `/api/v1/webhooks/flutterwave` | Flutterwave funding/payout events |

### Customer

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/users/me` | Current profile |
| `GET` | `/api/v1/users/me/summary` | Customer and wallet summary |
| `POST` | `/api/v1/kyc/submit` | Submit KYC |
| `GET` | `/api/v1/kyc/me` | View current KYC |
| `POST` | `/api/v1/funding/fund` | Initiate funding |
| `POST` | `/api/v1/transfers` | Transfer between wallets |
| `GET` | `/api/v1/wallets/transactions` | Filtered transaction history |
| `POST/GET` | `/api/v1/bank-accounts` | Save/list bank accounts |
| `GET` | `/api/v1/bank-accounts/default` | Get default bank account |
| `PATCH` | `/api/v1/bank-accounts/{id}/default` | Set default account |
| `DELETE` | `/api/v1/bank-accounts/{id}` | Delete account |
| `POST/GET` | `/api/v1/withdrawals` | Initiate/list withdrawals |
| `GET` | `/api/v1/ledger/transactions/{reference}` | Transaction ledger entries |

### Administration

| Method | Endpoint | Description |
| --- | --- | --- |
| `GET` | `/api/v1/admin/kyc/pending` | Pending KYC submissions |
| `POST` | `/api/v1/admin/kyc/{userId}/approve` | Approve KYC |
| `POST` | `/api/v1/admin/kyc/{userId}/reject` | Reject KYC |
| `POST` | `/api/v1/admin/users/{userId}/freeze` | Freeze user |
| `POST` | `/api/v1/admin/users/{userId}/unfreeze` | Unfreeze user |
| `GET` | `/api/v1/admin/users/{userId}/roles` | List roles |
| `POST` | `/api/v1/admin/users/{userId}/roles/grant` | Grant role |
| `POST` | `/api/v1/admin/users/{userId}/roles/revoke` | Revoke role |

## Example workflow

Amounts use the smallest currency unit: `100000` kobo is NGN 1,000.00.

```bash
# Register
curl -X POST http://localhost:8080/api/v1/users/register \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@example.com","phone":"08012345678","password":"strong-password","firstName":"Ada","lastName":"Lovelace"}'

# Log in; copy data.token from the response
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"ada@example.com","password":"strong-password"}'

# Initiate funding
curl -X POST http://localhost:8080/api/v1/funding/fund \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "Idempotency-key: fund-ada-001" \
  -d '{"amountInKobo":100000,"description":"Initial funding"}'

# Transfer to another ArionWallet account
curl -X POST http://localhost:8080/api/v1/transfers \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -H "Idempotency-key: transfer-ada-001" \
  -d '{"recipientAccountNumber":"0123456789","amountInKobo":25000,"currency":"NGN","narration":"Lunch"}'
```

Funding returns a provider payment URL; the wallet is credited only after a verified successful webhook.

To withdraw, first `POST /api/v1/bank-accounts` with `bankCode` and a 10-digit `accountNumber`, then send its returned UUID to `POST /api/v1/withdrawals` as `bankAccountId` along with `amountInKobo` and `currency`.

Transaction history accepts `page`, `size`, `type`, `status`, `startDate`, and `endDate`. Types are `FUNDING`, `TRANSFER`, `WITHDRAWAL`, and `ADJUSTMENT`; statuses are `PENDING`, `SUCCESS`, and `FAILED`.

## Idempotency

Funding, transfer, and withdrawal endpoints accept an optional `Idempotency-key` header. Generate one stable unique value per logical operation and reuse it only when retrying that same operation.

## Testing and building

```bash
# Test
./mvnw test

# Package and run
./mvnw clean package
java -jar target/arionwallet-0.0.1-SNAPSHOT.jar
```

Use `.\mvnw.cmd` on Windows. Tests cover application startup and core wallet, transfer, withdrawal, and fraud behavior.

## Security notes

- Rotate provider credentials that have ever been committed and remove them from history where appropriate.
- Store JWT and provider secrets in environment variables or a secrets manager.
- Use HTTPS; restrict Swagger, Actuator, and admin endpoints in deployed environments.
- Pin container versions instead of using `latest`.
- Replace schema auto-update with Flyway or Liquibase migrations.
- Add rate limiting, observability, reconciliation, and provider retry/dead-letter handling.
- Treat webhook verification, idempotency, ledger consistency, and concurrency tests as release-critical.

## Contributing

Create a focused branch, respect module boundaries, add tests for behavior changes, run the full suite, and never commit credentials, production data, or IDE files.