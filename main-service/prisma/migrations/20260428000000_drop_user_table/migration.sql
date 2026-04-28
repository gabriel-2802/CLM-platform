-- Drop foreign keys that reference the User table
ALTER TABLE "Task" DROP CONSTRAINT "Task_userId_fkey";
ALTER TABLE "UserClient" DROP CONSTRAINT "UserClient_userId_fkey";

-- Drop the User table (user management is now owned by user-service)
DROP TABLE "User";

-- Drop the Role enum (no longer needed in this schema)
DROP TYPE "Role";
