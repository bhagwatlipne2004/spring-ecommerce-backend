# Spring E-commerce Backend

A RESTful e-commerce backend built with **Java 21, Spring Boot, Spring Security, JWT authentication, Spring Data JPA, Hibernate, and PostgreSQL**.

The application provides APIs for user authentication, product and category management, shopping carts, orders, and product reviews. It is deployed on Render and exposes interactive API documentation through Swagger UI.

## Live Links

- **Live API:** https://spring-ecommerce-backend-1.onrender.com
- **Swagger UI:** https://spring-ecommerce-backend-1.onrender.com/swagger-ui/index.html
- **OpenAPI Specification:** https://spring-ecommerce-backend-1.onrender.com/v3/api-docs


## Features

### Authentication and Authorization
- User registration and login.
- JWT-based authentication for protected endpoints.
- Role-based access control for administrative product and order operations.

### Product Management
- Create, retrieve, update, and delete products.
- Retrieve products with pagination and filtering.
- Associate products with categories.

### Category Management
- Create and retrieve product categories.
- Store category descriptions, image URLs, and active status.

### Shopping Cart
- Add products to the cart.
- Update item quantities.
- Remove individual items or clear the cart.
- Retrieve the authenticated user's cart.

### Order Management
- Access order-related endpoints.
- Cancel orders.
- Update order status through an administrative endpoint.

### Product Reviews
- Retrieve product reviews.
- Submit, update, and delete reviews.

### API Documentation
- OpenAPI specification.
- Interactive Swagger UI for exploring and testing API endpoints.

### Deployment
- Deployed backend hosted on Render.
- PostgreSQL database for persistent storage.


## Tech Stack

| Technology | Purpose |
|---|---|
| Java 21 | Backend programming language |
| Spring Boot | Application framework and REST API development |
| Spring Security | Authentication and endpoint authorization |
| JWT | Token-based authentication |
| Spring Data JPA | Database access and persistence |
| Hibernate | ORM and entity management |
| PostgreSQL | Relational database |
| Maven | Dependency management and build automation |
| OpenAPI | API specification |
| Swagger UI | Interactive API documentation |
| Render | Backend deployment and hosting |

## Architecture

The application follows a layered backend architecture.

```text
Client / API Consumer
        |
        v
REST Controllers
        |
        v
Service Layer
        |
        v
Repository Layer
        |
        v
PostgreSQL Database
```

### Layer Responsibilities

- **Controller layer:** Receives HTTP requests and returns API responses.
- **Service layer:** Handles business logic and coordinates application operations.
- **Repository layer:** Performs database operations through Spring Data JPA.
- **Database layer:** Stores users, products, categories, carts, orders, order items, and reviews.

### Request Flow

1. A client sends an HTTP request to an API endpoint.
2. Spring Security checks authentication and authorization where required.
3. The controller receives the request and delegates the operation to the service layer.
4. The service layer interacts with repositories as needed.
5. The API returns an HTTP response to the client.


## API Endpoints

Base URL: `https://spring-ecommerce-backend-1.onrender.com`

### Authentication

| Method | Endpoint | Description | Authentication |
|---|---|---|---|
| POST | `/api/v1/auth/register` | Register a new user | Public |
| POST | `/api/v1/auth/login` | Log in and obtain a JWT | Public |

### Products

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/products` | Retrieve products with pagination and filtering |
| GET | `/api/v1/products/{id}` | Retrieve a product by ID |
| POST | `/api/v1/products` | Create a product |
| PUT | `/api/v1/products/{id}` | Update a product |
| DELETE | `/api/v1/products/{id}` | Delete a product |

Product creation, updating, and deletion require the appropriate administrative role.

### Categories

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/categories` | Retrieve categories |
| POST | `/api/v1/categories` | Create a category |

### Shopping Cart

| Method | Endpoint | Description |
|---|---|---|
| GET | `/api/v1/cart` | Retrieve the current user's cart |
| POST | `/api/v1/cart/items` | Add an item to the cart |
| PUT | `/api/v1/cart/items/{cartItemId}` | Update a cart item's quantity |
| DELETE | `/api/v1/cart/items/{cartItemId}` | Remove a cart item


## Authentication

The API uses JSON Web Tokens (JWT) to authenticate users on protected endpoints.

### 1. Register a User

`POST /api/v1/auth/register`

Example request:

```json
{
  "firstName": "John",
  "lastName": "Doe",
  "email": "john@example.com",
  "password": "ChangeMe123"
}
```

Use a password that satisfies the application's configured validation rules.

### 2. Log In

`POST /api/v1/auth/login`

Example request:

```json
{
  "email": "john@example.com",
  "password": "ChangeMe123"
}
```

The login response contains a JWT.

### 3. Access Protected Endpoints

In Swagger UI:

1. Register or log in using the authentication endpoints.
2. Copy the JWT returned by login.
3. Click **Authorize**.
4. Enter the token using the format expected by the Swagger bearer authentication scheme.
5. Execute a protected endpoint.

Never commit real JWTs, database credentials, or signing secrets to source control.

## Local Setup

### Prerequisites

- Java 21
- Maven (or the Maven Wrapper included in the repository)
- PostgreSQL
- Git

### 1. Clone the Repository

```bash
git clone https://github.com/bhagwatlipne2004/spring-commerce-backend.git
cd spring-commerce-backend
```

Confirm the repository URL matches your actual GitHub repository.

### 2. Configure Environment Variables

Configure the database connection and JWT signing secret using the environment variable names expected by the application:

```text
SPRING_PROFILES_ACTIVE=prod
SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/spring_ecommerce_db
SPRING_DATASOURCE_USERNAME=local_db_username
SPRING_DATASOURCE_PASSWORD=local_db_password
JWT_SECRET=
```

## Database Overview

The application uses PostgreSQL for relational data persistence.

The database schema includes the following tables:

- `users` — user account information
- `categories` — product categories
- `products` — product information
- `carts` — shopping carts
- `cart_items` — products and quantities in carts
- `orders` — order information
- `order_items` — products associated with orders
- `reviews` — product reviews

## Testing

### API Testing

Use Swagger UI to test the application's REST endpoints:

- Local: `http://localhost:8080/swagger-ui/index.html`
- Production: `https://spring-ecommerce-backend-1.onrender.com/swagger-ui/index.html`

Recommended manual test flow:

1. Register a user.
2. Log in and obtain a JWT.
3. Retrieve products and categories.
4. Authenticate and test cart operations.
5. Test order and review endpoints.
6. Verify validation errors and unauthorized requests.

Administrative operations should be tested using an appropriately authorized admin account.

### Automated Tests

Run the test suite with Maven:

```bash
mvn test
```

Automated test coverage should be documented once the tests and their results have been verified.

## Future Improvements

Potential areas for further development include:

- Automated integration tests for key API workflows
- Improved API error responses and validation documentation
- Database migration management
- CI/CD automation
- Caching for frequently accessed data
- Monitoring, logging, and production health checks

## Author

**Bhagwat Lipne**

- GitHub: https://github.com/bhagwatlipne2004
- LinkedIn: https://www.linkedin.com/in/bhagwat-lipne-035a4a321/

