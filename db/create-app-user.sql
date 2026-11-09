IF NOT EXISTS (SELECT 1 FROM sys.server_principals WHERE name = 'inventory_app')
    CREATE LOGIN inventory_app WITH PASSWORD = '$(DB_APP_PASSWORD)';
ELSE
    ALTER LOGIN inventory_app WITH PASSWORD = '$(DB_APP_PASSWORD)';

IF NOT EXISTS (SELECT 1 FROM sys.database_principals WHERE name = 'inventory_app')
   CREATE USER inventory_app FOR LOGIN inventory_app;

ALTER ROLE db_datareader ADD MEMBER inventory_app;
ALTER ROLE db_datawriter ADD MEMBER inventory_app;