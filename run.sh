#!/usr/bin/env bash

DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"
export DB_URL="jdbc:postgresql://localhost:5432/order_manager_db"
export DB_USER="postgres"
export DB_PASSWORD="postgres"

run() {
  bash "$DIR/gradlew" run
}

run
