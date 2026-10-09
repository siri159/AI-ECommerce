# AI-Powered Full Stack E-Commerce Platform

This project provides a complete Spring Boot backend for an AI-powered e-commerce platform. It is designed to work with a React frontend and supports product browsing, category management, carts, orders, reviews, AI shopping assistance, and admin operations.

## Features

- User registration and login with JWT authentication
- Role-based access control for customers and admins
- Product catalog with search, filtering, sorting, and pagination
- Categories and inventory management
- Cart and checkout workflows
- Order creation and status tracking
- Review submission with sentiment analysis
- AI shopping assistant and product recommendations
- Admin dashboard and sales insights
- Swagger UI and health check endpoints

## Architecture

The backend follows a layered design:

Controller -> Service -> Repository -> Entity -> MySQL

## Stack

- Java 21
- Spring Boot 4.1.1
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- MySQL
- Hibernate
- Bean Validation
- Lombok
- Swagger/OpenAPI
- Actuator

## Folder Structure

```text
src/
  main/
    java/com/ecommerce/backend/
      config/
      controller/
      dto/
      entity/
      exception/
      mapper/
      repository/
      security/
      service/
      service/impl/
      util/
    resources/
      application.properties
  test/
    java/
    resources/
```

## Environment Variables

Create a `.env` file based on `.env.example` before running the app.

```env
DB_URL=jdbc:mysql://localhost:3306/ecommerce?sslMode=REQUIRED
DB_USERNAME=your_db_username
DB_PASSWORD=your_db_password
AI_API_KEY=
AI_API_URL=
JWT_SECRET=replace-with-a-strong-secret-key
JWT_EXPIRATION=86400000
```

## Local Run

### Maven

```bash
./mvnw clean test
./mvnw spring-boot:run
```

### Docker

```bash
docker build -t ecommerce-backend .
docker run --env-file .env -p 8080:8080 ecommerce-backend
```

## Swagger and Health

- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI docs: http://localhost:8080/v3/api-docs
- Health check: http://localhost:8080/actuator/health

## Demo Credentials

Development-only credentials are initialized by the app:

- Admin: admin@ecommerce.com / Admin@123
- Customer: customer@ecommerce.com / Customer@123

## API Examples

- Register: POST /api/auth/register
- Login: POST /api/auth/login
- Products: GET /api/products
- Categories: GET /api/categories
- Cart: GET /api/cart
- Orders: POST /api/orders
- Reviews: POST /api/products/{productId}/reviews
- AI Chat: POST /api/ai/chat
- AI Recommendations: GET /api/ai/recommendations
- Admin Dashboard: GET /api/admin/dashboard

## Testing

```bash
./mvnw test
```

## Deployment Notes

- Use Aiven MySQL or a managed database for production.
- Keep secrets in environment variables.
- Add a real LLM integration behind the AI service interface when desired.

## Future Enhancements

- Real payment gateway integration
- Analytics dashboard with charts
- Recommendation personalization with historical data
- Role-specific admin actions and audit logs
- Async background processing for order events
