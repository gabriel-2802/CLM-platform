-- CreateEnum
CREATE TYPE "Role" AS ENUM ('ADMIN', 'USER', 'MODERATOR');

-- AlterTable
ALTER TABLE "User" ADD COLUMN     "rol" "Role" NOT NULL DEFAULT 'USER';
