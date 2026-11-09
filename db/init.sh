#!/bin/bash
set -e

SQLCMD="/opt/mssql-tools18/bin/sqlcmd -C -S db -U sa -P $MSSQL_SA_PASSWORD -b -I"

EXISTS=$($SQLCMD -h -1 -W -Q "SET NOCOUNT ON; SELECT COUNT(*) FROM sys.databases WHERE name = '$DB_NAME'")

if [ "$EXISTS" = "1" ]; then
    echo "Database $DB_NAME already exists - skipping scheme and seed."
  else
    echo "Creating database $DB_NAME..."
    $SQLCMD -Q "CREATE DATABASE [$DB_NAME]"

    echo "Running schema.sql..."
    $SQLCMD -d "$DB_NAME" -i /db/schema.sql

    echo "Running seed.sql..."
    $SQLCMD -d "$DB_NAME" -i /db/seed.sql
fi

echo "Creating application user..."
$SQLCMD -d "$DB_NAME" -i /db/create-app-user.sql;

echo "Database initialization finished."