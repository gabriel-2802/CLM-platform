FROM postgres:16-alpine

COPY db/postgresql.conf /etc/postgresql/postgresql.conf
COPY db/pg_hba.conf     /etc/postgresql/pg_hba.conf
COPY db/init-db.sql     /docker-entrypoint-initdb.d/01-init.sql

CMD ["postgres", "-c", "config_file=/etc/postgresql/postgresql.conf"]
