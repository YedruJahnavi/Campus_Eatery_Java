# Campus Eatery

> A campus food-ordering platform that lets students discover vendors, place orders, and track them live — while vendors and admins manage menus, orders, and approvals from a single dashboard.

---

## 1. Project Title

**Campus Eatery** — a full-stack campus food-ordering web application.

## 2. Project Description

Campus Eatery is a digital dining marketplace built for college campuses. Students can browse stalls and menu items, add items to a cart, check out with a delivery address, track their order status in real time, and leave reviews once their order is delivered. Vendors get a dashboard to manage their menu and update order status, while admins can approve vendor requests, manage users, and monitor platform stats.

**What it does**

- Student experience: vendor discovery, menu browsing, cart, checkout with address capture, live order tracking (WebSocket), reviews, and personalized recommendations.
- Vendor experience: menu management (add/edit/delete items) and order processing with status updates (PLACED → PREPARING → READY_FOR_PICKUP → DELIVERED).
- Admin experience: platform stats, user management, vendor request approval, and one-click demo data cleanup.

**Why the technologies used**

- **Spring Boot 4.1.0 (Java 17)** — provides a batteries-included backend with REST controllers, validation, and a first-class WebSocket implementation.
- **MongoDB (Spring Data MongoDB)** — flexible document schema that fits order, menu, and user data well and is easy to seed with demo content.
- **Spring Security + Clerk (OAuth2 Resource Server)** — handles JWT-based authentication out of the box, so students and vendors log in through Clerk without building our own auth system.
- **WebSocket / SockJS / STOMP** — pushes order status updates to students and vendors in real time.
- **Vanilla HTML/CSS/JS** — the frontend is served directly from `src/main/resources/static`, keeping the app simple to deploy and review.

**Challenges faced**

- Aligning the frontend payloads with the backend DTOs (e.g., a checkout address mismatch that silently failed all orders).
- Inconsistent order-status casing ("delivered" vs "DELIVERED") broke reviews, recommendations, and dashboards until a normalization layer was added.
- Making the admin panel work with credential-based login instead of requiring a Clerk JWT.
- Running the app requires a local MongoDB instance since no embedded database is configured.

**Features planned for the future**

- Rider features (delivery assignment, earnings, and payout tracking — models already exist).
- Online payments and payment history.
- A dedicated student order-history page.
- Hardened WebSocket topic authorization.

## 3. Table of Contents

- [Project Title](#1-project-title)
- [Project Description](#2-project-description)
- [Tech Stack](#tech-stack)
- [Project Structure](#project-structure)
- [How to Install and Run](#4-how-to-install-and-run-the-project)
- [How to Use the Project](#5-how-to-use-the-project)
- [API Overview](#api-overview)
- [Credits](#6-credits)
- [License](#7-license)

### Tech Stack

- **Backend:** Spring Boot 4.1.0, Java 17, Spring Data MongoDB, Spring Security, Spring WebSocket
- **Database:** MongoDB
- **Auth:** Clerk (OAuth2 resource server with JWT)
- **Frontend:** Vanilla HTML, CSS, JavaScript (served from static resources)
- **Build:** Maven (`mvnw`)

### Project Structure

```
Campus_Eatery_Java/
├── src/main/java/com/campuseatery/
│   ├── config/        # Security, WebSocket, data seeding, filters
│   ├── controller/    # REST API controllers
│   ├── dto/           # Request/response data objects
│   ├── model/         # MongoDB documents (Order, Stall, User, ...)
│   ├── repository/    # Spring Data MongoDB repositories
│   └── service/       # Business logic
└── src/main/resources/
    ├── application.yml # App configuration
    └── static/         # Frontend (HTML, CSS, JS)
```

## 4. How to Install and Run the Project

### Prerequisites

- **Java 17+** (JDK)
- **MongoDB** running locally on port 27017 (or set `MONGO_URI` to a remote/Atlas instance)
- **Maven** (optional — the project includes the `./mvnw` wrapper)

### Step 1 — Start MongoDB

If MongoDB is not already running, start it (this example uses a local data folder):

```bash
mkdir -p ~/mongo-data
mongod --dbpath ~/mongo-data --port 27017 --bind_ip 127.0.0.1
```

Alternatively, point the app at a MongoDB Atlas cluster via the `MONGO_URI` environment variable.

### Step 2 — Configure environment variables (optional)

| Variable | Default | Purpose |
| --- | --- | --- |
| `MONGO_URI` | `mongodb://localhost:27017/campus_eatery` | MongoDB connection string |
| `PORT` | `8080` | Server port |
| `ADMIN_USERNAME` | *(unset)* | Admin login username (required for admin login) |
| `ADMIN_PASSWORD` | *(unset)* | Admin login password (required for admin login) |

### Step 3 — Run the application

Using the Maven wrapper:

```bash
./mvnw spring-boot:run
```

Or build and run the jar:

```bash
./mvnw clean package -DskipTests
java -jar target/app.jar
```

Or with Docker:

```bash
docker build -t campus-eatery .
docker run -p 8080:8080 -e MONGO_URI=mongodb://host.docker.internal:27017/campus_eatery -e ADMIN_USERNAME=admin -e ADMIN_PASSWORD=admin123 campus-eatery
```

### Step 4 — Open the app

Once started, open <http://localhost:8080> in your browser. On first run, the app automatically seeds demo data (3 vendors, 8 menu items).

## 5. How to Use the Project

The platform has three roles: **Student**, **Vendor**, and **Admin**.

### Student

1. Open the home page and browse vendors and their menus.
2. Add items to your cart and open the cart drawer to review your order.
3. Check out — enter a delivery address (`address line 1`, `line 2`, `city`, `pincode`) and place the order.
4. Track the order in real time as the vendor updates its status.
5. Once delivered, leave a review and get personalized recommendations.

> Students and vendors authenticate through **Clerk**. Create an account via the Clerk login flow on the site; the app reads the JWT provided by Clerk.

### Vendor

1. Log in with your Clerk account (role `vendor`).
2. Open the vendor dashboard to add, edit, or remove menu items.
3. View incoming orders and update their status (PREPARING, READY_FOR_PICKUP, DELIVERED, etc.) — customers see the update live.

### Admin

The admin panel is reachable at <http://localhost:8080/admin.html> and only accepts requests from localhost (enforced by `AdminLocalhostFilter`).

**Default admin credentials** (must be configured via the `ADMIN_USERNAME` / `ADMIN_PASSWORD` environment variables):

| Username | Password |
| --- | --- |
| `admin` | `admin123` |

Admin capabilities:

- View platform stats (customers, orders, revenue, vendors).
- Manage users and vendor approval status.
- Approve or reject vendor requests.
- Clear all demo data (removes the seeded stalls and menu items in one click).

## API Overview

| Method | Endpoint | Description | Auth |
| --- | --- | --- | --- |
| GET | `/api/vendors` | List all vendors with menu items | Public |
| GET | `/api/vendors/{id}` | Vendor details | Public |
| GET | `/api/vendors/{stallId}/menu` | Vendor menu | Public |
| POST | `/api/vendors/menu` | Add menu item | JWT (vendor) |
| PUT | `/api/vendors/menu/{id}` | Update menu item | JWT (vendor) |
| DELETE | `/api/vendors/menu/{id}` | Delete menu item | JWT (vendor) |
| POST | `/api/vendor-request` | Submit vendor registration request | Public |
| POST | `/api/cart/add` | Add item to cart | JWT |
| PUT | `/api/cart/{itemId}` | Update cart item | JWT |
| DELETE | `/api/cart/{itemId}` | Remove cart item | JWT |
| POST | `/api/order/checkout` | Place an order | JWT |
| PUT | `/api/order/{id}/status` | Update order status | JWT (vendor) |
| GET | `/api/user/me` | Current user profile | JWT |
| PUT | `/api/user/profile` | Update profile | JWT |
| GET | `/api/user/address` | Get delivery address | JWT |
| PUT | `/api/user/address` | Save delivery address | JWT |
| GET | `/api/user/recommendations` | Personalized recommendations | JWT |
| POST | `/api/reviews` | Submit a review | JWT |
| POST | `/api/admin/login` | Admin login | Public |
| GET | `/api/admin/stats` | Platform stats | Admin |
| GET | `/api/admin/users` | List users | Admin |
| PUT | `/api/admin/users/{id}/status` | Update user status | Admin |
| GET | `/api/admin/vendor-requests` | List vendor requests | Admin |
| POST | `/api/admin/approve-vendor/{id}` | Approve a vendor | Admin |
| DELETE | `/api/admin/demo-data` | Clear demo data | Admin |
| WS | `/ws` | WebSocket for live order updates | Public |

## 6. Credits

Campus Eatery was developed by:

- [YedruJahnavi](https://github.com/YedruJahnavi) — project owner

Resources used in this project:

- [Spring Boot Reference Documentation](https://docs.spring.io/spring-boot/)
- [Spring Security](https://spring.io/projects/spring-security)
- [Clerk](https://clerk.com) — authentication
- [MongoDB](https://www.mongodb.com) — database

## 7. License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for details.

---

### 8. Badges

![Java](https://img.shields.io/badge/Java-17-orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.1.0-brightgreen)
![MongoDB](https://img.shields.io/badge/MongoDB-8.0-green)
![Maven](https://img.shields.io/badge/Maven-3-blue)
![License](https://img.shields.io/badge/License-MIT-yellow)