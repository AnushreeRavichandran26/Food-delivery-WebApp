# 🍽️ Food Delivery WebApp

A full-stack web application for online food ordering and delivery management. Built with Spring Boot backend and vanilla JavaScript frontend.

---

## 📋 Table of Contents

1. [Project Description](#project-description)
2. [Tech Stack](#tech-stack)
3. [Features](#features)
4. [Project Structure](#project-structure)
5. [MVC Architecture](#mvc-architecture)
6. [Database ERD](#database-erd)
7. [How to Run](#how-to-run)

---

## 📝 Project Description

Food Delivery WebApp is a comprehensive platform that connects customers, restaurants, and delivery partners. Users can browse restaurants, add items to their cart, place orders with multiple payment options, and track their deliveries in real-time. The application provides restaurant owners with order management capabilities and admins with system oversight.

### Key Objectives

- Enable seamless food ordering experience for customers
- Provide restaurants with efficient order management
- Automate delivery agent assignment and tracking
- Ensure secure payment processing
- Maintain user data with strong security practices

---

## 🛠️ Tech Stack

### Backend
- **Framework:** Spring Boot 3.1.5
- **Language:** Java 17+
- **Database:** MySQL 8.0
- **ORM:** Hibernate/JPA
- **Security:** Spring Security with BCrypt password hashing
- **Build Tool:** Maven 3.x
- **API:** RESTful API with JSON responses

### Frontend
- **Markup:** HTML5 (semantic)
- **Styling:** CSS3 (Flexbox/Grid)
- **Scripting:** Vanilla JavaScript (no frameworks)
- **HTTP Client:** Fetch API
- **Serving:** npm serve

### Development Tools
- Git for version control
- Maven for dependency management
- MySQL Workbench or CLI for database management

---

## ✨ Features

### Customer Features
- User registration and secure login with email verification
- Browse restaurants by cuisine, ratings, and location
- View detailed menu with food items, images, and prices
- Add/remove items to/from shopping cart
- Multiple payment options: Credit Card, UPI, Cash on Delivery
- Real-time order tracking from kitchen to doorstep
- View complete order history with past orders
- Update profile information and manage delivery addresses
- Contact delivery agent information on active orders

### Restaurant Features
- Restaurant profile creation and management
- Add, edit, and delete menu items with images
- Receive real-time order notifications
- Manage order status and kitchen operations
- View ratings and customer reviews

### Delivery Partner Features
- Automatic order assignment based on location
- View assigned delivery orders
- Update delivery status in real-time

### Admin Features
- Manage all restaurants in the system
- Monitor all orders across restaurants
- Track delivery agents and their performance
- View system analytics and reports

---

## 📁 Project Structure

```
Food-delivery-WebApp/
├── src/
│   ├── main/
│   │   ├── java/com/fooddelivery/
│   │   │   ├── controller/
│   │   │   │   ├── UserController.java
│   │   │   │   ├── RestaurantController.java
│   │   │   │   ├── MenuItemController.java
│   │   │   │   ├── OrderController.java
│   │   │   │   └── PaymentController.java
│   │   │   ├── service/
│   │   │   │   ├── UserService.java
│   │   │   │   ├── RestaurantService.java
│   │   │   │   ├── MenuItemService.java
│   │   │   │   ├── OrderService.java
│   │   │   │   └── PaymentService.java
│   │   │   ├── repository/
│   │   │   │   ├── UserRepository.java
│   │   │   │   ├── RestaurantRepository.java
│   │   │   │   ├── MenuItemRepository.java
│   │   │   │   ├── OrderRepository.java
│   │   │   │   └── PaymentRepository.java
│   │   │   ├── model/
│   │   │   │   ├── User.java
│   │   │   │   ├── Restaurant.java
│   │   │   │   ├── MenuItem.java
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── Payment.java
│   │   │   ├── config/
│   │   │   │   └── SecurityConfig.java
│   │   │   ├── exception/
│   │   │   │   └── GlobalExceptionHandler.java
│   │   │   └── FoodDeliveryApplication.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           └── index.html   ← 🧠 Main frontend file served by Spring Boot
│   └── test/
│       └── java/ (unit tests)
│
├── database/
│   ├── schema.sql (database tables)
│   └── sample-data.sql (initial data)
│
├── frontend/
│   ├── index.html
│   ├── css/
│   │   ├── style.css
│   │   └── responsive.css
│   ├── js/
│   │   ├── app.js
│   │   ├── api.js
│   │   ├── auth.js
│   │   └── cart.js
│   └── assets/
│       └── images/
│
├── pom.xml (Maven dependencies)
└── README.md

```

---

## 🏗️ MVC Architecture

The application follows the Model-View-Controller (MVC) architectural pattern:

```
┌─────────────────────────────────────────────┐
│         View Layer (Frontend)               │
│    HTML, CSS, JavaScript (Vanilla)          │
│    Running on localhost:3000                │
└──────────────────┬──────────────────────────┘
                   │ HTTP Requests (Fetch API)
                   ▼
┌─────────────────────────────────────────────┐
│      Controller Layer (REST API)            │
│    @RestController                          │
│    Handles HTTP requests & responses        │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│      Service Layer (Business Logic)         │
│    @Service                                 │
│    Contains core application logic          │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│      Repository Layer (Data Access)        │
│    @Repository / JpaRepository              │
│    Communicates with database               │
└──────────────────┬──────────────────────────┘
                   │
                   ▼
┌─────────────────────────────────────────────┐
│         MySQL Database                      │
│    Persistent data storage                  │
└─────────────────────────────────────────────┘
```

**Data Flow:**
1. Frontend sends HTTP request to backend API
2. Controller receives request and delegates to Service layer
3. Service implements business logic and delegates to Repository
4. Repository performs database operations
5. Response flows back through the layers to the frontend
6. Frontend updates UI based on the response

---

## 🗄️ Database ERD

```
┌─────────────────────────────────────────────────────┐
│                    USERS                            │
├─────────────────────────────────────────────────────┤
│ • id (PK)              • phone                      │
│ • name                 • address                    │
│ • email (UNIQUE)       • city                       │
│ • password             • postal_code               │
│ • created_at           • updated_at                │
└──────────────┬──────────────────────────────────────┘
               │ 1:N
               ▼
┌─────────────────────────────────────────────────────────────────┐
│                      ORDERS                                     │
├─────────────────────────────────────────────────────────────────┤
│ • id (PK)                  • payment_method                     │
│ • user_id (FK) ──────┐     • estimated_delivery                │
│ • restaurant_id (FK) │     • delivery_agent                    │
│ • total_amount       │     • created_at                        │
│ • tax                │     • updated_at                        │
│ • delivery_fee       ├──→ (Links to USERS 1:N)                 │
│ • status             ├──→ (Links to RESTAURANTS 1:N)           │
│ • delivery_address   │
└──────┬────────────────────────────┬────────────────────────────┘
       │ 1:N                        │ 1:1
       ▼                            ▼
┌──────────────────────┐    ┌──────────────────────────┐
│   ORDER_ITEMS        │    │     PAYMENTS             │
├──────────────────────┤    ├──────────────────────────┤
│ • id (PK)            │    │ • id (PK)                │
│ • order_id (FK)      │    │ • order_id (FK) UNIQUE  │
│ • menu_item_id (FK)  │    │ • amount                 │
│ • quantity           │    │ • payment_method        │
│ • price              │    │ • payment_status        │
└────────┬─────────────┘    │ • transaction_id        │
         │ N:1              │ • created_at            │
         ▼                  └──────────────────────────┘
┌──────────────────────────┐
│    MENU_ITEMS            │
├──────────────────────────┤
│ • id (PK)                │
│ • restaurant_id (FK)     │
│ • name                   │
│ • description            │
│ • price                  │
│ • created_at             │
└────────┬─────────────────┘
         │ N:1
         ▼
┌──────────────────────────┐
│   RESTAURANTS            │
├──────────────────────────┤
│ • id (PK)                │
│ • name                   │
│ • cuisine                │
│ • rating                 │
│ • image_url              │
│ • created_at             │
└──────────────────────────┘

┌──────────────────────────┐
│  DELIVERY_AGENTS         │
├──────────────────────────┤
│ • id (PK)                │
│ • name                   │
│ • phone                  │
│ • rating                 │
│ • created_at             │
└──────────────────────────┘
```

**Key Relationships:**

| Relationship | Description |
|:-----------|:-----------|
| Users → Orders | 1:N - One user can place multiple orders |
| Restaurants → Orders | 1:N - One restaurant fulfills multiple orders |
| Restaurants → Menu Items | 1:N - One restaurant has multiple menu items |
| Orders → Order Items | 1:N - One order contains multiple items |
| Menu Items → Order Items | 1:N - Menu items appear in multiple orders |
| Orders → Payments | 1:1 - Each order has exactly one payment record |
| Delivery Agents (standalone) | Independent table for delivery partner management |

---

## 🚀 How to Run

### Prerequisites

Before running the application, ensure you have:
- **Java JDK 17+** installed
- **Maven 3.6+** installed
- **MySQL 8.0+** installed and running
- **Node.js & npm** installed
- **Git** (optional, for cloning)

### Step-by-Step Setup

#### 1. **Clone the Repository**

```bash
git clone https://github.com/AnushreeRavichandran26/Food-delivery-WebApp.git
cd Food-delivery-WebApp
```

#### 2. **Setup MySQL Database**

Open MySQL command line or MySQL Workbench and run:

```bash
mysql -u root -p
```

Enter your MySQL password when prompted. Then execute:

```sql
CREATE DATABASE food_delivery;
USE food_delivery;
```

#### 3. **Load Database Schema**

Run the schema.sql file from the database folder:

```bash
mysql -u root -p food_delivery < database/schema.sql
```

(Optional) Load sample data if you want initial data:

```bash
mysql -u root -p food_delivery < database/sample-data.sql
```

#### 4. **Configure Database Connection**

Edit `src/main/resources/application.properties` and update:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/food_delivery
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect

# Server Configuration
server.port=8080
server.servlet.context-path=/api
```

Replace `your_mysql_password` with your actual MySQL password.

#### 5. **Run Backend Server**

In the project root directory, execute:

```bash
mvn clean spring-boot:run
```

The backend will start on `http://localhost:8080/api`

**Expected Output:**
```
Started FoodDeliveryApplication in X.XXX seconds
```

#### 6. **Run Frontend Server** (in a new terminal)

Navigate to the frontend directory:

```bash
cd index.html
npm install
npx serve
```

The frontend will start on `http://localhost:3000`

#### 7. **Access the Application**

Open your browser and go to:

```
http://localhost:3000
```

You should see the Food Delivery WebApp homepage.

### All Commands Summary

**Terminal 1 (Backend):**
```bash
mvn clean spring-boot:run
```

**Terminal 2 (Frontend):**
```bash
cd frontend
npx serve
```

**Browser:**
```
http://localhost:3000
```

---

## 🔗 API Base URL

```
http://localhost:8080/api
```

## 📚 Common Endpoints

- `POST /api/users/register` - Register new user
- `POST /api/users/login` - User login
- `GET /api/restaurants` - Get all restaurants
- `GET /api/restaurants/{id}/menu` - Get restaurant menu
- `POST /api/orders` - Create new order
- `GET /api/orders/{id}` - Get order details

---

## 🆘 Troubleshooting

**Issue:** Backend won't start
- Check if MySQL is running
- Verify database credentials in application.properties
- Ensure port 8080 is not in use

**Issue:** Frontend won't load
- Make sure backend is running first
- Clear browser cache and refresh
- Check if port 3000 is available

**Issue:** Database connection error
- Verify MySQL username and password
- Ensure `food_delivery` database exists
- Run schema.sql again

---

