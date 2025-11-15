Taller5-AYGO — Minimal Message Feed with Zero Trust JWT Authorization

Overview
--------
This project is a minimal Spring Boot web application used in a workshop to practice Zero Trust security principles. It implements a simple message feed: a small static web client (HTML + JS + CSS) sends messages to a Spring Boot backend which stores them in memory and returns the latest messages for display.

This README documents the architecture, API, security design (Zero Trust), how to run the project, and how to test it locally.

Architecture
------------
- Client: static files served from src/main/resources/static (index.html, login.html). The client is a plain HTML + JavaScript single page that:
  - Lets a user enter a message into a text field.
  - Asynchronously POSTs the message to the backend (/message) and displays returned messages.
- Backend: Spring Boot application (src/main/java/edu/eci/taller5)
  - Controller: `MessageController` exposes endpoints:
    - GET /        -> serves the client HTML (index.html)
    - GET /app-login -> serves a login page (login.html) used as the public client entry point
    - POST /message -> accepts a message payload and returns the current in-memory message map
  - Model: `Message` JSON object with fields:
    - message: string (the message text sent by the client)
    - clientIp: string (IP address extracted from the incoming request)
    - timestamp: server-side timestamp (LocalDateTime)
  - Storage: in-memory ConcurrentHashMap<String, Message> keyed by timestamp.toString() (keeps all messages for that run; the client can display last N messages)

API / Data Flow
---------------
1. Client loads GET / (index.html) and shows a simple UI (message text field + send button).
2. On send, the client issues a POST /message with the message body (plain string JSON in this repo's controller).
3. Server receives the message, constructs a `Message` with server time and client IP, stores it in the in-memory map, and returns the full message map as JSON.

Message JSON example

{
  "message": "Hello world",
  "clientIp": "127.0.0.1",
  "timestamp": "2025-11-15T12:34:56.789"
}

Response for POST /message: Map<String, Message>
- The controller returns a JSON object mapping timestamp (string) -> Message. The client can sort or read the last 10 items to show the most recent.

Security and Zero Trust design
------------------------------
This project was developed in two phases (as required by the workshop):
1. No security — to focus on basic functionality and client/backend integration.
2. Secure the backend using JWT-based authorization and Zero Trust principles.

How Zero Trust is applied here
- Never trust, always verify: Every protected API request (POST /message and other API endpoints) requires a validated JWT access token. The server does strict token validation.
- Authentication & authorization per request: The server enforces authentication for every request using Spring Security. There is no implicit trust based on network location.
- Minimal surface exposure: Only the public client endpoints are exposed without auth (/ and /app-login). The message API is protected.
- Separation of concerns: Public client assets (static HTML/JS) are served separately from the protected API endpoints. OAuth2/OIDC client configuration (for interactive login) is separate from the resource-server (JWT validation).

Implementation details
- Spring Security configuration lives in `SecurityConfig`. It registers both OAuth2 client support (for browser-based login flows) and a resource server for JWT validation:
  - .oauth2Login(...) enables interactive login flows for the web client.
  - .oauth2ResourceServer().jwt() enables JWT validation for API requests.
- The code expects these properties (set them in `application.properties` or environment):
  - spring.security.oauth2.resourceserver.jwt.issuer-uri = <OIDC issuer URI>
  - auth0.audience = <expected audience claim for tokens>
  - okta.oauth2.client-id = <client id used for logout redirect> (or other provider property name as appropriate)

Running & Testing
-----------------

```bash
curl -X POST http://localhost:8080/message \
  -H "Authorization: Bearer <ACCESS_TOKEN>" \
  -H "Content-Type: text/plain" \
  --data "Hello from curl"
```

If you call POST /message without a valid token you should receive a 401 unauthorized response (that demonstrates per-request authentication enforcement).

