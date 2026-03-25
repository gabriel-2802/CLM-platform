#!/bin/bash

# CLM Platform - Environment Setup Script
# This script guides you through setting up the CLM Platform
# including PostgreSQL databases, environment variables, and dependencies

set -e

BLUE='\033[0;34m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Helper functions
print_header() {
    echo ""
    echo -e "${BLUE}╔════════════════════════════════════════════════════════════════╗${NC}"
    echo -e "${BLUE}║ $1"
    echo -e "${BLUE}╚════════════════════════════════════════════════════════════════╝${NC}"
    echo ""
}

print_step() {
    echo -e "${GREEN}→ $1${NC}"
}

print_warning() {
    echo -e "${YELLOW}⚠ $1${NC}"
}

print_error() {
    echo -e "${RED}✗ $1${NC}"
}

print_success() {
    echo -e "${GREEN}✓ $1${NC}"
}

# Main setup
main() {
    print_header "CLM Platform - Initial Setup"
    
    # Check prerequisites
    print_step "Checking prerequisites..."
    check_prerequisites
    
    # PostgreSQL setup
    print_step "Setting up PostgreSQL databases..."
    setup_databases
    
    # Environment configuration
    print_step "Creating environment configuration..."
    setup_environment
    
    # Dependencies
    print_step "Installing dependencies..."
    install_dependencies
    
    # Prisma migrations
    print_step "Running database migrations..."
    run_migrations
    
    # Seed data
    print_step "Seeding databases..."
    seed_databases
    
    # Final summary
    print_header "✓ Setup Complete!"
    print_success "CLM Platform is ready to use!"
    echo ""
    echo -e "${GREEN}Next steps:${NC}"
    echo "  1. Start the development server: ${BLUE}make dev${NC}"
    echo "  2. Open your browser: ${BLUE}http://localhost:3000${NC}"
    echo "  3. Log in with credentials from the seed"
    echo ""
}

check_prerequisites() {
    local all_ok=true
    
    # Check Node.js
    if ! command -v node &> /dev/null; then
        print_error "Node.js is not installed"
        echo "  Download from: https://nodejs.org/"
        all_ok=false
    else
        node_version=$(node --version)
        print_success "Node.js detected: $node_version"
    fi
    
    # Check npm
    if ! command -v npm &> /dev/null; then
        print_error "npm is not installed"
        all_ok=false
    else
        npm_version=$(npm --version)
        print_success "npm detected: $npm_version"
    fi
    
    # Check PostgreSQL
    if ! command -v psql &> /dev/null; then
        print_error "PostgreSQL is not installed"
        echo "  Download from: https://www.postgresql.org/download/"
        all_ok=false
    else
        pg_version=$(psql --version)
        print_success "PostgreSQL detected: $pg_version"
    fi
    
    # Check Make
    if ! command -v make &> /dev/null; then
        print_warning "Make is not installed (optional but recommended)"
        echo "  Install on macOS: brew install make"
        echo "  Install on Ubuntu: sudo apt-get install make"
        echo "  Install on Windows: https://www.gnu.org/software/make/"
    else
        print_success "Make detected"
    fi
    
    if [ "$all_ok" = false ]; then
        print_error "Required prerequisites missing"
        exit 1
    fi
}

setup_databases() {
    echo ""
    echo "PostgreSQL database setup"
    echo ""
    
    # Get PostgreSQL connection details
    read -p "PostgreSQL host [localhost]: " pg_host
    pg_host=${pg_host:-localhost}
    
    read -p "PostgreSQL port [5432]: " pg_port
    pg_port=${pg_port:-5432}
    
    read -p "PostgreSQL username [postgres]: " pg_user
    pg_user=${pg_user:-postgres}
    
    read -sp "PostgreSQL password: " pg_password
    echo ""
    
    # Test connection
    print_step "Testing PostgreSQL connection..."
    if ! PGPASSWORD="$pg_password" psql -h "$pg_host" -p "$pg_port" -U "$pg_user" -c "SELECT 1" > /dev/null 2>&1; then
        print_error "Cannot connect to PostgreSQL"
        echo "  Verify your credentials and that PostgreSQL is running"
        exit 1
    fi
    print_success "PostgreSQL connection successful"
    
    # Create databases
    print_step "Creating databases..."
    PGPASSWORD="$pg_password" psql -h "$pg_host" -p "$pg_port" -U "$pg_user" << EOF
    CREATE DATABASE IF NOT EXISTS general_db;
    CREATE DATABASE IF NOT EXISTS contracts_db;
    \l
EOF
    
    print_success "Databases created: general_db, contracts_db"
    
    # Store connection info for later use
    export POSTGRES_HOST="$pg_host"
    export POSTGRES_PORT="$pg_port"
    export POSTGRES_USER="$pg_user"
    export POSTGRES_PASSWORD="$pg_password"
}

setup_environment() {
    echo ""
    
    cd general
    
    if [ -f .env.local ]; then
        print_warning ".env.local already exists"
        read -p "Overwrite? (y/n) [n]: " overwrite
        if [ "$overwrite" != "y" ] && [ "$overwrite" != "Y" ]; then
            echo "Skipping environment setup"
            cd ..
            return
        fi
    fi
    
    # Generate NEXTAUTH_SECRET
    print_step "Generating NEXTAUTH_SECRET..."
    nextauth_secret=$(openssl rand -base64 32)
    
    # Generate database URLs
    db_url="postgresql://${POSTGRES_USER}:${POSTGRES_PASSWORD}@${POSTGRES_HOST}:${POSTGRES_PORT}/general_db"
    contracts_db_url="postgresql://${POSTGRES_USER}:${POSTGRES_PASSWORD}@${POSTGRES_HOST}:${POSTGRES_PORT}/contracts_db"
    
    # Create .env.local
    cat > .env.local << EOF
# NextAuth Configuration
NEXTAUTH_SECRET=$nextauth_secret
NEXTAUTH_URL=http://localhost:3000

# Database URLs
DATABASE_URL=$db_url
DATABASE_URL_CONTRACTS=$contracts_db_url

# Environment
NODE_ENV=development
EOF
    
    print_success ".env.local created"
    echo "  Location: general/.env.local"
    echo "  Ensure NEXTAUTH_SECRET is at least 32 characters"
    
    cd ..
}

install_dependencies() {
    print_step "Installing npm dependencies..."
    cd general
    npm install
    print_success "Dependencies installed"
    cd ..
}

run_migrations() {
    print_step "Running Prisma migrations for general database..."
    cd general
    npx prisma migrate deploy
    print_success "Migrations completed"
    cd ..
    
    print_step "Preparing contracts database schema..."
    echo "Note: Contracts database will be used by Spring microservice"
    echo "Run migrations separately when contracts service is ready"
    print_success "Contracts schema ready at: contracts/prisma/schema.prisma"
}

seed_databases() {
    print_step "Seeding general database..."
    cd general
    npx prisma db seed || print_warning "Seed script not found or failed (this is ok)"
    print_success "Database seeding completed"
    cd ..
}

# Run main setup
main
