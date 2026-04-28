/*
  Warnings:

  - You are about to drop the column `platitorTVA` on the `Client` table. All the data in the column will be lost.
  - The `salariati` column on the `Client` table would be dropped and recreated. This will lead to data loss if there is data in the column.
  - Added the required column `DaLunarTrim` to the `Client` table without a default value. This is not possible if the table is not empty.

*/
-- CreateEnum
CREATE TYPE "DaLunarTrim" AS ENUM ('DA_LUNAR', 'DA_TRIM', 'NU');

-- AlterTable
ALTER TABLE "Client" DROP COLUMN "platitorTVA",
ADD COLUMN     "DaLunarTrim" "DaLunarTrim" NOT NULL,
ALTER COLUMN "impozit" DROP NOT NULL,
ALTER COLUMN "tvaLaIncasare" DROP NOT NULL,
ALTER COLUMN "areCodTVAUE" DROP NOT NULL,
ALTER COLUMN "operatiuneUE" DROP NOT NULL,
ALTER COLUMN "dividende" DROP NOT NULL,
DROP COLUMN "salariati",
ADD COLUMN     "salariati" "DaLunarTrim",
ALTER COLUMN "casaDeMarcat" DROP NOT NULL;

-- DropEnum
DROP TYPE "public"."PlatitorTVA";
