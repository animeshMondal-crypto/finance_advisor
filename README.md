# Finance Advisor

An intelligent personal finance management system powered by **Spring Boot** and **Google Gemini AI**, designed specifically for Indian users. Track accounts, transactions, budgets, and get AI-powered financial insights with natural language support.

## 🎯 Features

### Core Finance Management
- **Account Management**: Create and manage multiple accounts (savings, checking, investment, etc.)
- **Transaction Tracking**: Log income, expenses, and transfers with automatic AI categorization
- **Category Management**: Organize transactions into customizable categories
- **Budget Rules**: Set monthly budgets per category with configurable alert thresholds
- **Balance Transfers**: Transfer funds between accounts with automatic transaction logging

### AI-Powered Insights
- **Smart Categorization**: AI automatically categorizes transactions based on descriptions
- **Monthly Summaries**: AI-generated monthly financial summaries
- **Anomaly Detection**: Detect unusual spending patterns
- **Budget Suggestions**: Get personalized spending recommendations
- **Natural Language Queries**: Ask questions about your finances in plain English (e.g., "How much did I spend on groceries last month?")

### Security & Compliance
- **JWT Authentication**: Secure login and token-based authentication
- **User Isolation**: Each user's data is completely isolated
- **Audit Logging**: Track all financial operations for compliance
- **Optimistic Locking**: Prevent concurrent update conflicts
- **Input Validation**: Comprehensive request validation

### India-Specific Features
- **INR Currency Support**: Default currency set to Indian Rupees (₹)
- **IST Timezone**: Configured for Indian Standard Time
- **PostgreSQL Database**: Enterprise-grade data persistence

---

## 🚀 Getting Started

### Prerequisites
- **Java 21** or higher
- **Maven 3.6+**
- **PostgreSQL 17+**
- **Docker** (optional, for database setup)
- **Google Gemini API Key** ([Get one here](https://ai.google.dev/))

### Installation

#### 1. Clone the Repository
```bash
git clone https://github.com/animeshMondal-crypto/finance_advisor.git
cd finance_advisor
```

#### 2. Set Up PostgreSQL Database

**Option A: Using Docker Compose (Recommended)**
```bash
docker-compose up -d
```

**Option B: Manual PostgreSQL Setup**
```bash
# Create database
createdb finance_advisor

# Import schema (if provided)
psql -U postgres -d finance_advisor -f docker/postgres/init.sql
```



**Set Environment Variables:**
```bash
# Linux/Mac
export GOOGLE_GENAI_API_KEY="your-gemini-api-key"
export JWT_SECRET="your-jwt-secret-key"

# Windows PowerShell
$env:GOOGLE_GENAI_API_KEY = "your-gemini-api-key"
$env:JWT_SECRET = "your-jwt-secret-key"
```

#### 4. Build and Run

```bash
# Build with Maven
mvn clean package

# Run the application
mvn spring-boot:run

# Or run the JAR directly
java -jar target/financeadvisor-0.0.1-SNAPSHOT.jar
```

The application will start on **http://localhost:8080**

---

## 📚 API Documentation

### Authentication

#### Register User
```http
POST /api/auth/register
Content-Type: application/json

{
  "name": "John Doe",
  "email": "john@example.com",
  "password": "SecurePassword@123"
}
```

**Response:**
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "email": "john@example.com",
    "token": "eyJhbGc..."
  }
}
```

#### Login
```http
POST /api/auth/login
Content-Type: application/json

{
  "email": "john@example.com",
  "password": "SecurePassword@123"
}
```

---

### Accounts

#### Create Account
```http
POST /api/accounts
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Savings Account",
  "type": "SAVINGS",
  "initialBalance": 50000
}
```

#### List All Accounts
```http
GET /api/accounts
Authorization: Bearer {token}
```

#### Get Account Details
```http
GET /api/accounts/{id}
Authorization: Bearer {token}
```

#### Update Account
```http
PUT /api/accounts/{id}?name=Updated Name
Authorization: Bearer {token}
```

#### Transfer Funds
```http
POST /api/accounts/transfer
Authorization: Bearer {token}
Content-Type: application/json

{
  "fromAccountId": 1,
  "toAccountId": 2,
  "amount": 5000,
  "description": "Monthly transfer"
}
```

#### Delete Account
```http
DELETE /api/accounts/{id}
Authorization: Bearer {token}
```

---

### Transactions

#### Log Transaction
```http
POST /api/transactions
Authorization: Bearer {token}
Content-Type: application/json

{
  "accountId": 1,
  "type": "DEBIT",
  "amount": 500,
  "description": "Grocery shopping at Walmart",
  "occurredAt": "2026-07-29T15:30:00"
}
```

**AI Categorization**: The transaction description is automatically analyzed by Gemini AI to assign the most relevant category.

#### Get Transactions
```http
GET /api/transactions?from=2026-07-01T00:00:00&to=2026-07-31T23:59:59
Authorization: Bearer {token}
```

#### Get Transaction Summary
```http
GET /api/transactions/summary
Authorization: Bearer {token}
```

**Returns**: Total income, expenses, net change by category

#### Update Category (Override AI Assignment)
```http
PATCH /api/transactions/{id}/category
Authorization: Bearer {token}
Content-Type: application/json

{
  "categoryId": 3
}
```

---

### Budgets

#### Create Budget
```http
POST /api/budgets
Authorization: Bearer {token}
Content-Type: application/json

{
  "categoryId": 1,
  "monthlyLimit": 10000,
  "alertThresholdPct": 80
}
```

#### List All Budgets
```http
GET /api/budgets
Authorization: Bearer {token}
```

#### Get Budget Details
```http
GET /api/budgets/{id}
Authorization: Bearer {token}
```

#### Update Budget
```http
PUT /api/budgets/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "monthlyLimit": 12000,
  "alertThresholdPct": 75
}
```

#### Monthly Budget Rollover
```http
POST /api/budgets/rollover
Authorization: Bearer {token}
```

**Effect**: Resets monthly spending counters for all budgets and generates alerts if thresholds were exceeded last month.

#### Delete Budget
```http
DELETE /api/budgets/{id}
Authorization: Bearer {token}
```

---

### Categories

#### Create Category
```http
POST /api/categories
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Entertainment",
  "description": "Movies, games, subscriptions"
}
```

#### List Categories
```http
GET /api/categories
Authorization: Bearer {token}
```

#### Update Category
```http
PUT /api/categories/{id}
Authorization: Bearer {token}
Content-Type: application/json

{
  "name": "Updated Category Name"
}
```

#### Delete Category
```http
DELETE /api/categories/{id}
Authorization: Bearer {token}
```

---

### AI Insights

#### Get All Insights
```http
GET /api/insights
Authorization: Bearer {token}
```

#### Get Monthly Summary
```http
GET /api/insights/summary
Authorization: Bearer {token}
```

**Returns**: AI-generated overview of spending patterns and financial health for current month

#### Get Anomalies
```http
GET /api/insights/anomalies
Authorization: Bearer {token}
```

**Returns**: AI-detected unusual spending patterns or transactions

#### Get Suggestions
```http
GET /api/insights/suggestions
Authorization: Bearer {token}
```

**Returns**: Personalized budgeting and spending recommendations

#### Get Insights by Month
```http
GET /api/insights/month/2026-07
Authorization: Bearer {token}
```

#### Generate Specific Insight
```http
POST /api/insights/generate?type=MONTHLY_SUMMARY
Authorization: Bearer {token}
```

**Types**: `MONTHLY_SUMMARY`, `ANOMALY`, `SUGGESTION`, `BUDGET_ALERT`

#### Natural Language Query
```http
POST /api/insights/query
Authorization: Bearer {token}
Content-Type: application/json

{
  "question": "How much did I spend on groceries last month?"
}
```

**Response**: AI-generated answer based on your transaction history

---

### Audit Logs

#### Get Audit Trail
```http
GET /api/audit
Authorization: Bearer {token}
```

**Returns**: Complete history of all financial operations (creates, updates, deletes)

---

## 🏗️ Project Structure

```
finance_advisor/
├── src/main/java/com/krypto/financeadvisor/
│   ├── config/              # Spring configuration (AI, Security)
│   ├── controller/          # REST API endpoints
│   ├── service/             # Business logic
│   │   ├── ai/             # AI-powered services
│   │   └── interfaces/     # Service contracts
│   ├── entity/             # JPA entities (User, Account, Transaction, etc.)
│   ├── repository/         # Spring Data repositories
│   ├── dto/                # Request/Response DTOs
│   ├── security/           # JWT authentication
│   ├── exception/          # Custom exceptions
│   └── util/               # Utility classes
├── src/main/resources/
│   ├── application.yml     # Application configuration
│   └── init.sql           # Database initialization
├── docker/
│   └── postgres/          # PostgreSQL setup
└── pom.xml               # Maven dependencies
```

---

## 🔧 Technology Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 21 |
| **Framework** | Spring Boot 4.1.0 |
| **Database** | PostgreSQL 17 |
| **AI** | Google Gemini 2.0 Flash |
| **Authentication** | JWT + Spring Security |
| **Validation** | Jakarta Bean Validation |
| **Serialization** | Jackson JSON |
| **ORM** | Hibernate JPA |
| **Build** | Maven |
| **Containerization** | Docker & Docker Compose |

---

## 💾 Database Schema

### Core Entities

**users**
- User accounts with email, password hash, and currency preference

**accounts**
- Multiple financial accounts per user (SAVINGS, CHECKING, INVESTMENT, etc.)
- Optimistic locking for concurrent transaction safety
- Balance tracking with precision up to 2 decimal places

**transactions**
- Income, expense, and transfer records
- AI categorization tracking
- Transfer pair linking for dual-leg transfers
- Indexed for fast queries

**categories**
- Transaction categories for organization
- User-defined or system-provided

**budget_rules**
- Monthly spending limits per category
- Dynamic alert threshold tracking
- Month-end rollover capability

**ai_insights**
- Stored AI-generated insights
- Types: MONTHLY_SUMMARY, ANOMALY, SUGGESTION, BUDGET_ALERT
- Indexed by user and month for efficient retrieval

**audit_logs**
- Complete transaction audit trail
- Compliance and debugging

---

## 🔐 Security Features

### Authentication
- **JWT Tokens**: Stateless, token-based authentication
- **Password Hashing**: Bcrypt password hashing (configured in `AuthService`)
- **CORS**: Cross-Origin Resource Sharing configured

### Authorization
- **Role-Based Access**: Each endpoint requires authentication
- **User Isolation**: Users can only access their own data
- **Resource Ownership Checks**: Controllers verify user owns requested resources

### Data Protection
- **Encryption**: API keys stored as environment variables
- **Optimistic Locking**: Prevents lost updates in concurrent scenarios
- **SQL Injection Prevention**: Parameterized queries via JPA
- **CSRF Protection**: Enabled in Spring Security

---

## 🧠 AI Integration

### Google Gemini AI Features

1. **Transaction Categorization**
   - Analyzes transaction descriptions
   - Assigns categories automatically
   - Fallback to manual categorization available

2. **Monthly Summaries**
   - Generates natural language summaries
   - Highlights spending trends
   - Identifies key financial events

3. **Anomaly Detection**
   - Detects unusual transactions
   - Identifies spending spikes
   - Alerts on suspicious patterns

4. **Budget Suggestions**
   - Recommends budget adjustments
   - Proposes realistic spending limits
   - Based on historical data

5. **Natural Language Queries**
   - Ask questions about finances naturally
   - Supports multi-step queries
   - Returns contextual answers


---

## 🧪 Testing

Run tests with Maven:

```bash
# Run all tests
mvn test

# Run specific test class
mvn test -Dtest=UserServiceTest

# Run with coverage
mvn test jacoco:report
```

Test classes are located in `src/test/java/`

---

## 🚨 Error Handling

The API uses consistent error responses:

```json
{
  "success": false,
  "message": "Error description",
  "data": null
}
```

### Common HTTP Status Codes

| Status | Meaning |
|--------|---------|
| 200 | Success |
| 201 | Created |
| 400 | Bad Request |
| 401 | Unauthorized |
| 403 | Forbidden |
| 404 | Not Found |
| 409 | Conflict (e.g., concurrent update) |
| 500 | Server Error |

### Custom Exceptions

- `ResourceNotFoundException`: Requested resource not found
- `InsufficientFundsException`: Account has insufficient balance
- `DuplicateResourceException`: Resource already exists
- `InvalidTransactionException`: Invalid transaction parameters

---

## 📊 Example Workflows

### Workflow 1: Monthly Budget Review

1. **User logs in**
   ```bash
   POST /api/auth/login
   ```

2. **Get accounts**
   ```bash
   GET /api/accounts
   ```

3. **Get transaction summary**
   ```bash
   GET /api/transactions/summary
   ```

4. **Get monthly AI summary**
   ```bash
   GET /api/insights/summary
   ```

5. **Check budgets and anomalies**
   ```bash
   GET /api/budgets
   GET /api/insights/anomalies
   ```

### Workflow 2: AI-Powered Transaction Logging

1. User logs a transaction with description
   ```bash
   POST /api/transactions
   {
     "description": "Spent at Dunzo for groceries"
   }
   ```

2. AI analyzes and categorizes as "Groceries"

3. Budget tracking updates automatically

4. If spending exceeds threshold, budget alert is generated

5. User can query: "How much on groceries this month?"
   ```bash
   POST /api/insights/query
   {
     "question": "How much on groceries this month?"
   }
   ```

---

## 🐛 Troubleshooting

### PostgreSQL Connection Issues
```
Error: FATAL: Ident authentication failed for user "postgres"
Solution: Check database credentials in application.yml
```

### Google Gemini API Errors
```
Error: Invalid API key
Solution: Verify GOOGLE_GENAI_API_KEY environment variable is set correctly
```

### JWT Token Expired
```
Error: 401 Unauthorized
Solution: Login again to get a new token (expires in 24 hours)
```

### Concurrent Update Conflicts
```
Error: 409 Conflict (OptimisticLockException)
Solution: Application retries automatically or user can retry the request
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit changes (`git commit -m 'Add amazing feature'`)
4. Push to branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 👤 Author

**Animesh Mondal**
- GitHub: [@animeshMondal-crypto](https://github.com/animeshMondal-crypto)
- Repository: [finance_advisor](https://github.com/animeshMondal-crypto/finance_advisor)

---

## 💬 Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Check existing documentation
- Review error logs for detailed error messages

---

## 🎓 Key Learning Resources

- [Spring Boot Documentation](https://spring.io/projects/spring-boot)
- [Google Gemini API Guide](https://ai.google.dev/)
- [PostgreSQL Documentation](https://www.postgresql.org/docs/)
- [JWT Authentication](https://jwt.io/)
- [Spring Security](https://spring.io/projects/spring-security)

---

**Happy budgeting! 💰**
