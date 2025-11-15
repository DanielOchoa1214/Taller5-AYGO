# Taller5-AYGO — Minimal Message Feed with Zero Trust JWT Authorization

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

Data Flow
---------------

Here is a high level design of how the proyect inforces zero trust: 
<img width="1571" height="485" alt="ZeroTrustTaller5" src="https://github.com/user-attachments/assets/ba50337c-32d7-47d3-853d-22d0e3cf3287" />

The app flow is something like this: 

1. Client loads GET /app-login (login.html) and shows a simple UI (Login link).

<img width="495" height="158" alt="image" src="https://github.com/user-attachments/assets/58616e84-b27a-4e94-abc0-6d82fab69c4b" />

2. On login the user is redirected to the auth0 login page:

<img width="435" height="607" alt="image" src="https://github.com/user-attachments/assets/c2ce20f3-a0eb-40cd-979e-3ce1bbdee01f" />

(Note: Normally the user would be requested to login with his personal account but I was allready logged in by this point)

3. On login the user is redirected to the main page:

<img width="371" height="144" alt="image" src="https://github.com/user-attachments/assets/efd5d66a-11d2-4e56-9ca0-9265e090efc0" />

4. Here the use can type any message. On send, the client issues a POST /message with the the Bearer token and the message body.
6. Server receives the message, constructs a `Message` with server time and client IP, stores it in the in-memory map, and returns the full message map as JSON.

<img width="407" height="252" alt="image" src="https://github.com/user-attachments/assets/3c8480aa-1ba7-4af7-b9ca-b6add11ae050" />

Message JSON example

{
  "message": "Hello world",
  "clientIp": "127.0.0.1",
  "timestamp": "2025-11-15T12:34:56.789"
}

Response for POST /message: Map<String, Message>
- The controller returns a JSON object mapping timestamp (string) -> Message. The client can sort or read the last 10 items to show the most recent.

Via the browser the client automatically sets the token, but if we try to call the API via Postman without it we can see how all endpoints (except login) are rejected by default: 

<img width="714" height="424" alt="image" src="https://github.com/user-attachments/assets/b3819903-409f-4514-9e8b-9e8412f7dc9c" />
<img width="713" height="449" alt="image" src="https://github.com/user-attachments/assets/ba1f020d-48ba-44b3-8d6d-b60ef82f32ed" />
<img width="699" height="633" alt="image" src="https://github.com/user-attachments/assets/2446c88b-b491-4d03-942c-8b7d008f56cb" />

Conclusions
------------------------------

We successfully applied Zero Trust by following their principles in the following ways: 
- Authentication & authorization per request: The server enforces authentication for every request using Spring Security. All requests validate the JWT
- Minimal surface exposure: Only the public client endpoints are exposed without auth (/app-login). The message API and main page are protected.
- Never trust, always verify: Every protected API request (POST /message and GET / are protected) requires a validated JWT access token. The server does strict token validation.

