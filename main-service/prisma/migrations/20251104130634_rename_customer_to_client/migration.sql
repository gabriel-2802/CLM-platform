/*
  Warnings:

  - You are about to drop the column `customerId` on the `Detalii` table. All the data in the column will be lost.
  - You are about to drop the column `customerId` on the `Istoric` table. All the data in the column will be lost.
  - You are about to drop the column `customerId` on the `PunctDeLucru` table. All the data in the column will be lost.
  - You are about to drop the column `customerId` on the `Task` table. All the data in the column will be lost.
  - You are about to drop the `Customer` table. If the table is not empty, all the data it contains will be lost.
  - You are about to drop the `UserCustomer` table. If the table is not empty, all the data it contains will be lost.
  - A unique constraint covering the columns `[clientId]` on the table `Detalii` will be added. If there are existing duplicate values, this will fail.
  - A unique constraint covering the columns `[clientId]` on the table `Istoric` will be added. If there are existing duplicate values, this will fail.
  - A unique constraint covering the columns `[clientId]` on the table `PunctDeLucru` will be added. If there are existing duplicate values, this will fail.
  - A unique constraint covering the columns `[clientId]` on the table `Task` will be added. If there are existing duplicate values, this will fail.
  - Added the required column `clientId` to the `Detalii` table without a default value. This is not possible if the table is not empty.
  - Added the required column `clientId` to the `Istoric` table without a default value. This is not possible if the table is not empty.
  - Added the required column `clientId` to the `PunctDeLucru` table without a default value. This is not possible if the table is not empty.
  - Added the required column `clientId` to the `Task` table without a default value. This is not possible if the table is not empty.

*/
-- AlterEnum
ALTER TYPE "Administratie" ADD VALUE 'VRANCEA';

-- DropForeignKey
ALTER TABLE "public"."Detalii" DROP CONSTRAINT "Detalii_customerId_fkey";

-- DropForeignKey
ALTER TABLE "public"."Istoric" DROP CONSTRAINT "Istoric_customerId_fkey";

-- DropForeignKey
ALTER TABLE "public"."PunctDeLucru" DROP CONSTRAINT "PunctDeLucru_customerId_fkey";

-- DropForeignKey
ALTER TABLE "public"."Task" DROP CONSTRAINT "Task_customerId_fkey";

-- DropForeignKey
ALTER TABLE "public"."UserCustomer" DROP CONSTRAINT "UserCustomer_customerId_fkey";

-- DropForeignKey
ALTER TABLE "public"."UserCustomer" DROP CONSTRAINT "UserCustomer_userId_fkey";

-- DropIndex
DROP INDEX "public"."Detalii_customerId_key";

-- DropIndex
DROP INDEX "public"."Istoric_customerId_key";

-- DropIndex
DROP INDEX "public"."PunctDeLucru_customerId_key";

-- DropIndex
DROP INDEX "public"."Task_customerId_key";

-- AlterTable
ALTER TABLE "Detalii" DROP COLUMN "customerId",
ADD COLUMN     "clientId" INTEGER NOT NULL;

-- AlterTable
ALTER TABLE "Istoric" DROP COLUMN "customerId",
ADD COLUMN     "clientId" INTEGER NOT NULL;

-- AlterTable
ALTER TABLE "PunctDeLucru" DROP COLUMN "customerId",
ADD COLUMN     "clientId" INTEGER NOT NULL;

-- AlterTable
ALTER TABLE "Task" DROP COLUMN "customerId",
ADD COLUMN     "clientId" INTEGER NOT NULL;

-- DropTable
DROP TABLE "public"."Customer";

-- DropTable
DROP TABLE "public"."UserCustomer";

-- CreateTable
CREATE TABLE "Client" (
    "id" SERIAL NOT NULL,
    "denumire" TEXT NOT NULL,
    "tip" "Tip" NOT NULL,
    "cui" TEXT NOT NULL,
    "activa" BOOLEAN NOT NULL,
    "dataVerificarii" TIMESTAMP(3),
    "adresa" TEXT,
    "administratie" "Administratie" NOT NULL,
    "impozit" "Impozit" NOT NULL,
    "platitorTVA" "PlatitorTVA" NOT NULL,
    "tvaLaIncasare" BOOLEAN NOT NULL,
    "areCodTVAUE" BOOLEAN NOT NULL,
    "codTVAUE" TEXT,
    "operatiuneUE" BOOLEAN NOT NULL,
    "dividende" BOOLEAN NOT NULL,
    "salariati" "PlatitorTVA" NOT NULL,
    "casaDeMarcat" BOOLEAN NOT NULL,
    "dataExpSediuSocial" TIMESTAMP(3),
    "dataExpMandatAdmin" TIMESTAMP(3),
    "dataCertificatFiscal" TIMESTAMP(3),
    "dataFisaPlatitor" TIMESTAMP(3),
    "dataVectFiscal" TIMESTAMP(3),
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Client_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "UserClient" (
    "id" SERIAL NOT NULL,
    "userId" INTEGER NOT NULL,
    "clientId" INTEGER NOT NULL,

    CONSTRAINT "UserClient_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "Client_cui_key" ON "Client"("cui");

-- CreateIndex
CREATE UNIQUE INDEX "UserClient_userId_clientId_key" ON "UserClient"("userId", "clientId");

-- CreateIndex
CREATE UNIQUE INDEX "Detalii_clientId_key" ON "Detalii"("clientId");

-- CreateIndex
CREATE UNIQUE INDEX "Istoric_clientId_key" ON "Istoric"("clientId");

-- CreateIndex
CREATE UNIQUE INDEX "PunctDeLucru_clientId_key" ON "PunctDeLucru"("clientId");

-- CreateIndex
CREATE UNIQUE INDEX "Task_clientId_key" ON "Task"("clientId");

-- AddForeignKey
ALTER TABLE "Detalii" ADD CONSTRAINT "Detalii_clientId_fkey" FOREIGN KEY ("clientId") REFERENCES "Client"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "PunctDeLucru" ADD CONSTRAINT "PunctDeLucru_clientId_fkey" FOREIGN KEY ("clientId") REFERENCES "Client"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Istoric" ADD CONSTRAINT "Istoric_clientId_fkey" FOREIGN KEY ("clientId") REFERENCES "Client"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Task" ADD CONSTRAINT "Task_clientId_fkey" FOREIGN KEY ("clientId") REFERENCES "Client"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "UserClient" ADD CONSTRAINT "UserClient_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "UserClient" ADD CONSTRAINT "UserClient_clientId_fkey" FOREIGN KEY ("clientId") REFERENCES "Client"("id") ON DELETE CASCADE ON UPDATE CASCADE;
