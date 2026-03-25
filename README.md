# CLM Platform

**Contabilitate Ledger Management Platform** - A comprehensive accounting and contract management system built with modern technologies.

## 🎯 Overview

CLM Platform is a multi-service application designed to manage:
- **Client/Company Management**: Track business entities with detailed compliance information
- **Accounting & Tasks**: Manage accounting tasks, deadlines, and automation rules
- **Contract Management**: Handle contracts with signatures, versions, and reminders (via Spring microservice)
- **User Management**: Role-based access control (ADMIN, MANAGER, USER)

## 🏗️ Architecture

### Single Database with Two Schemas & Microservices

The platform uses **one PostgreSQL database with two separate Prisma schemas**, managed by two independent services:

```
┌─────────────────────────────────────────────────────────────┐
│                    CLM Platform                              │
├──────────────────────┬──────────────────────────────────────┤
│   Next.js Frontend  │      Spring Boot Contracts Service     │
│   (React + Node.js) │      (Java microservice)               │
├──────────────────────┼──────────────────────────────────────┤
│         clm_platform database                               │
├──────────────────────┬──────────────────────────────────────┤
│   public schema      │      contracts schema                 │
│ (Accounting Data)    │    (Contract Data)                    │
│ (Prisma/Next.js)     │  (Spring Boot JPA/Hibernate)         │
└─────────────────────────────────────────────────────────────┘
```

**Architecture Benefits:**
- **Single database instance**: Simplified deployment and backup
- **Schema separation**: Clean logical boundaries between services
- **Independent services**: Each can be deployed and scaled separately
- **Technology flexibility**: Next.js/Node.js for accounting, Spring Boot for contracts
- **Clear responsibilities**: Accounting API handles public schema, Contracts API handles contracts schema

### Directory Structure

```
CLM-platform/
├── Makefile                    # Setup and management commands
├── SETUP_GUIDE.md              # Detailed setup instructions
├── setup.sh                    # Automated setup script
├── README.md                   # This file
│
├── general/                    # Main Next.js Application
│   ├── app/                    # Next.js app directory (routes & pages)
│   │   ├── api/                # API routes
│   │   ├── (auth)/             # Authentication routes
│   │   ├── clients/            # Client management UI
│   │   ├── dashboard/          # Main dashboard
│   │   ├── situatie/           # Financial statements
│   │   ├── taskuri/            # Task management
│   │   └── users/              # User management
│   ├── components/             # React components (reusable)
│   │   ├── auth/               # Auth components
│   │   ├── clients/            # Client-related components
│   │   ├── dashboard/          # Dashboard components
│   │   ├── ui/                 # Base UI components (Shadcn)
│   │   └── ...
│   ├── lib/                    # Utilities & helpers
│   │   ├── auth/               # NextAuth configuration
│   │   ├── generated/          # Generated Prisma client
│   │   └── utils.ts            # Utility functions
│   ├── prisma/                 # Database layer (General DB)
│   │   ├── schema.prisma       # Data models
│   │   ├── seed.ts             # Initial data seeding
│   │   └── migrations/         # Database migrations
│   ├── actions/                # Server actions (Next.js 13+)
│   ├── hooks/                  # React hooks
│   ├── types/                  # TypeScript types
│   ├── public/                 # Static assets
│   ├── package.json            # Dependencies
│   ├── .env.example            # Environment variables template
│   ├── .env.local              # Environment variables (local, not committed)
│   ├── next.config.ts          # Next.js configuration
│   ├── tsconfig.json           # TypeScript configuration
│   └── .gitignore
│
├── contracts/                  # Spring Boot Contracts Microservice (Future)
│   ├── prisma/                 # Database layer (Contracts DB)
│   │   ├── schema.prisma       # Contract data models
│   │   └── migrations/         # Database migrations
│   ├── src/                    # Java source code (future)
│   └── README.md               # Contracts service documentation
│
├── agent/                      # Python Monitoring Agent
│   ├── monitor_folder.py       # Folder monitoring functionality
│   ├── prompt.py               # Prompt engineering
│   ├── requirements.txt        # Python dependencies
│   └── README.md               # Agent documentation
│
└── .git/                       # Git repository
```

## 🚀 Quick Start

### ⚡ Fastest Setup (Recommended - With Docker)

```bash
# 1. Start PostgreSQL in Docker
make docker-up

# 2. Install dependencies & run setup
make install && make migrate && make seed

# 3. Start development server
make dev
```

Open: **http://localhost:3000**

For detailed Docker instructions, see [DOCKER_SETUP.md](DOCKER_SETUP.md).

### Alternative: Manual Setup (Without Docker)

See [SETUP_GUIDE.md](SETUP_GUIDE.md) for manual installation instructions.

## 📋 Prerequisites

Before starting, ensure you have:
- **Node.js** v18+ (https://nodejs.org/)
- **npm** or **yarn**
- **PostgreSQL** v14+ (https://www.postgresql.org/download/)
- **Make** (optional, for `make` commands)

Verify installation:
```bash
node --version    # v18.0.0 or higher
npm --version     # v8.0.0 or higher
psql --version    # PostgreSQL version
```

## 🗄️ Database Schema

### Single PostgreSQL Database: `clm_platform`

The database contains **two Prisma schemas** for logical separation:

#### Public Schema (General Application)

**Core Tables:**
- **User**: Application users with roles (ADMIN, MANAGER, USER)
- **Client**: Business entities/companies being managed
- **Detalii**: Detailed client information (compliance, documents)
- **PunctDeLucru**: Work locations/branches for clients
- **Istoric**: Historical financial data by year
- **Task**: Task management and tracking
- **Rule**: Automation rules for task generation
- **RuleCondition**: Rule conditions

**User Roles:**
- **ADMIN**: Full access to all features and data
- **MANAGER**: Can manage assigned clients and teams
- **USER**: Limited access to assigned tasks only

#### Contracts Schema

Reserved for the Spring Boot contracts microservice:
- Managed by Spring Boot application using JPA/Hibernate ORM
- Will contain: Contracts, Versions, Signatures, Templates, Amendments, Reminders, Attachments (to be defined)
- Connects to the same `clm_platform` database via JDBC/Spring Data JPA
- Can be deployed independently from the Next.js application

## 🛠️ Development

### Start Development Server

```bash
# Using Make
make dev

# Or manually
cd general
npm run dev
```

Access the app at: **http://localhost:3000**

### Database Management

```bash
# Open Prisma Studio (visual DB explorer)
cd general
npx prisma studio

# Create a new migration
npx prisma migrate dev --name descriptive_name

# Apply pending migrations
npx prisma migrate deploy

# Reset database (CAUTION: deletes all data)
npx prisma migrate reset

# View logs
make logs
```

### Available Commands

```bash
# Development
make dev              # Start dev server
make build            # Build for production
make start            # Start production server

# Database
make setup-db         # Create databases
make migrate          # Run migrations
make seed             # Seed data
make reset-db         # Reset databases

# Maintenance
make clean            # Clean builds and node_modules
make reset            # Full reset (clean + reinstall + reset DB)
make logs             # Show setup information
make help             # Show all available commands
```

## 🔒 Authentication

The platform uses **NextAuth.js** with JWT tokens for authentication:

```typescript
// Roles are stored in JWT token
// Available roles: ADMIN, MANAGER, USER

// Check user role in server actions:
const session = await getSession();
const userRole = session?.user?.role;

if (userRole === 'ADMIN') {
  // Admin-only operations
}
```

## 🔄 API Routes

Key API endpoints:

```
POST   /api/auth/signin          # User login
POST   /api/auth/register        # User registration
POST   /api/auth/callback/jwt    # NextAuth JWT callback

GET    /api/users                # Get all users (admin only)
GET    /api/clients              # Get assigned clients
POST   /api/clients              # Create new client

GET    /api/tasks                # Get tasks
POST   /api/tasks                # Create task

GET    /api/auth/session         # Get current session
```

## 📊 Key Features

- ✅ **Role-Based Access Control**: ADMIN, MANAGER, USER roles
- ✅ **Client Management**: Complete company information tracking
- ✅ **Task Automation**: Rules-based task generation with conditions
- ✅ **Financial Data**: Historical financial information tracking
- ✅ **Compliance Tracking**: Document expiry and certification dates
- ✅ **Multi-database Support**: Separate databases for different services
- ✅ **JWT Authentication**: Secure token-based authentication
- ✅ **Responsive UI**: Mobile-friendly interface with Shadcn components

## 📦 Tech Stack

### Frontend
- **Framework**: Next.js 15 with TypeScript
- **UI Library**: React 19 with Shadcn components
- **Styling**: Tailwind CSS
- **Tables**: TanStack React Table
- **Icons**: Tabler Icons & Lucide

### Backend - Accounting Service (Node.js)
- **Runtime**: Node.js with Next.js API routes
- **Authentication**: NextAuth.js 4 with JWT
- **ORM**: Prisma 5 (manages `public` schema)
- **Database**: PostgreSQL (shared `clm_platform`)
- **Password Hashing**: bcryptjs

### Backend - Contracts Service (Spring Boot)
- **Framework**: Spring Boot 3 (Java)
- **ORM**: JPA/Hibernate (manages `contracts` schema)
- **Database**: PostgreSQL (shared `clm_platform`)
- **Authentication**: JWT / OAuth2 (integrates with NextAuth)
- **API**: REST with Spring Data JPA
- **Build**: Maven or Gradle
- **Deployment**: Independent microservice

### Shared Infrastructure
- **Database**: PostgreSQL with two schemas (public, contracts)
- **Number of databases**: 1 (`clm_platform`)
- **Message Queue** (optional): For async contract notifications
- **Logging**: Centralized log aggregation

## 🔧 Configuration

### Environment Variables

Create `general/.env.local`:

```env
# NextAuth
NEXTAUTH_SECRET=generate-with-openssl-rand-base64-32
NEXTAUTH_URL=http://localhost:3000

# Database
DATABASE_URL=postgresql://user:password@localhost:5432/general_db
DATABASE_URL_CONTRACTS=postgresql://user:password@localhost:5432/contracts_db

NODE_ENV=development
```

For production, use `.env.production`.

## 🐛 Troubleshooting

### PostgreSQL Connection Error
```
Error: Cannot connect to PostgreSQL at localhost:5432
```

**Solution:**
```bash
# Check if PostgreSQL is running
psql -U postgres -h localhost -c "SELECT 1"

# Check DATABASE_URL in .env.local
# Verify port and credentials
```

### Port 3000 Already in Use
```bash
# Kill process on port 3000
lsof -ti:3000 | xargs kill -9

# Or use different port
cd general && PORT=3001 npm run dev
```

### Database Migration Errors
```bash
# Reset database and retry
cd general
npx prisma migrate reset --force
npx prisma db seed
```

## 📚 Documentation

- [SETUP_GUIDE.md](SETUP_GUIDE.md) - Detailed setup instructions
- [general/README.md](general/README.md) - Next.js app documentation
- [contracts/README.md](contracts/README.md) - Contracts service documentation
- [agent/README.md](agent/README.md) - Python agent documentation

## 🤝 Contributing

1. Create a feature branch
2. Make your changes
3. Test thoroughly
4. Submit a pull request

## 📝 License

[Your License Here]

## 📞 Support

For issues or questions:
1. Check [SETUP_GUIDE.md](SETUP_GUIDE.md) troubleshooting section
2. Review Prisma Studio: `npx prisma studio`
3. Check logs: `make logs`
4. Create an issue with details

---

**Happy coding! 🚀**
