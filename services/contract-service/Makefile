DB_CONTAINER  := clm-test-pg
DB_PORT       := 5434
DB_NAME       := clm_test
DB_USER       := clm_user
DB_PASS       := clm_pass

.PHONY: test-cache-real

# Start a temporary PostgreSQL container, run the real-DB cache integration
# test, then remove the container — whether the test passed or failed.
test-cache-real:
	@echo "→ starting $(DB_CONTAINER) on port $(DB_PORT)"
	@docker run -d --name $(DB_CONTAINER) \
	  -e POSTGRES_DB=$(DB_NAME) \
	  -e POSTGRES_USER=$(DB_USER) \
	  -e POSTGRES_PASSWORD=$(DB_PASS) \
	  -p $(DB_PORT):5432 \
	  postgres:16-alpine
	@echo "→ waiting for PostgreSQL to accept connections"
	@until docker exec $(DB_CONTAINER) pg_isready -U $(DB_USER) -d $(DB_NAME) -q; do sleep 1; done
	@echo "→ running CacheRealDbTest"
	@mvn test -Dtest=CacheRealDbTest --no-transfer-progress; \
	 EXIT=$$?; \
	 echo "→ removing $(DB_CONTAINER)"; \
	 docker rm -f $(DB_CONTAINER); \
	 exit $$EXIT
