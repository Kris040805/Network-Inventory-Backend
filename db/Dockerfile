FROM mcr.microsoft.com/mssql/server:2022-latest

COPY init.sh schema.sql seed.sql create-app-user.sql /db/

ENTRYPOINT ["/bin/bash", "/db/init.sh"]