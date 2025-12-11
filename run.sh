#!/bin/bash

# VertebralCare - One-click Setup and Run Script
# Usage: ./run.sh

set -e

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$SCRIPT_DIR"

echo "========================================"
echo "  VertebralCare Setup & Run Script"
echo "========================================"
echo ""

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Function to print colored messages
info() { echo -e "${GREEN}[INFO]${NC} $1"; }
warn() { echo -e "${YELLOW}[WARN]${NC} $1"; }
error() { echo -e "${RED}[ERROR]${NC} $1"; }

# ========== Step 1: Check Environment ==========
info "Checking environment..."

# Check Java
if ! command -v java &> /dev/null; then
    error "Java is not installed. Please install Java 8 or higher."
    exit 1
fi
JAVA_VERSION=$(java -version 2>&1 | head -n 1)
info "Java: $JAVA_VERSION"

# Check Maven
if ! command -v mvn &> /dev/null; then
    error "Maven is not installed. Please install Maven."
    echo "  brew install maven"
    exit 1
fi
MVN_VERSION=$(mvn -version 2>&1 | head -n 1)
info "Maven: $MVN_VERSION"

# Check MySQL client
if ! command -v mysql &> /dev/null; then
    error "MySQL client is not installed."
    echo "  brew install mysql"
    exit 1
fi
info "MySQL client: installed"

echo ""

# ========== Step 2: Start MySQL Service ==========
info "Checking MySQL service..."

# Try to connect to MySQL to see if it's running
if mysql -u root -e "SELECT 1" &> /dev/null 2>&1; then
    info "MySQL is running (no password)"
    MYSQL_PASSWORD=""
elif mysql -u root -p'' -e "SELECT 1" &> /dev/null 2>&1; then
    info "MySQL is running (empty password)"
    MYSQL_PASSWORD=""
else
    # MySQL might not be running or needs password
    warn "MySQL is not accessible. Attempting to start..."

    # Try different methods to start MySQL
    if command -v brew &> /dev/null && brew services list 2>/dev/null | grep -q mysql; then
        info "Starting MySQL via Homebrew..."
        brew services start mysql 2>/dev/null || true
        sleep 3
    elif command -v mysql.server &> /dev/null; then
        info "Starting MySQL via mysql.server..."
        mysql.server start 2>/dev/null || true
        sleep 3
    else
        warn "Could not auto-start MySQL. Please start it manually."
    fi
fi

echo ""

# ========== Step 3: Get Database Password ==========
info "Database configuration..."

# Ask for MySQL root password
echo -n "Enter MySQL root password (press Enter if empty): "
read -s MYSQL_PASSWORD
echo ""

# Test connection
if [ -z "$MYSQL_PASSWORD" ]; then
    if ! mysql -u root -e "SELECT 1" &> /dev/null 2>&1; then
        error "Cannot connect to MySQL with empty password."
        exit 1
    fi
else
    if ! mysql -u root -p"$MYSQL_PASSWORD" -e "SELECT 1" &> /dev/null 2>&1; then
        error "Cannot connect to MySQL with provided password."
        exit 1
    fi
fi

info "MySQL connection successful!"
echo ""

# ========== Step 4: Initialize Database ==========
info "Initializing database..."

if [ -z "$MYSQL_PASSWORD" ]; then
    mysql -u root < src/main/resources/init.sql
else
    mysql -u root -p"$MYSQL_PASSWORD" < src/main/resources/init.sql
fi

info "Database 'vertebral_db' initialized successfully!"
echo ""

# ========== Step 5: Update db.properties ==========
info "Updating database configuration..."

DB_PROPS="src/main/resources/db.properties"

# Create or update db.properties
cat > "$DB_PROPS" << EOF
# MySQL Database Configuration
db.url=jdbc:mysql://localhost:3306/vertebral_db?useSSL=false&serverTimezone=UTC&characterEncoding=UTF-8
db.user=root
db.password=$MYSQL_PASSWORD

# Connection pool settings (optional)
db.maxPoolSize=10
db.minPoolSize=2
EOF

info "Configuration saved to $DB_PROPS"
echo ""

# ========== Step 6: Compile Project ==========
info "Compiling project with Maven..."
mvn clean compile -q

if [ $? -eq 0 ]; then
    info "Compilation successful!"
else
    error "Compilation failed!"
    exit 1
fi

echo ""

# ========== Step 7: Run Application ==========
info "Starting VertebralCare application..."
echo ""
echo "========================================"
echo "  Application is starting..."
echo "  Close the window to exit."
echo "========================================"
echo ""

mvn exec:java -Dexec.mainClass="com.vertebralcare.Main" -q

echo ""
info "Application closed."
