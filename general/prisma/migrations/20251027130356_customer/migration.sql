-- CreateEnum
CREATE TYPE "Tip" AS ENUM ('SRL', 'PFA', 'II', 'ASOC');

-- CreateEnum
CREATE TYPE "DaNuNuECazul" AS ENUM ('DA', 'NU', 'NU_E_CAZUL');

-- CreateEnum
CREATE TYPE "Impozit" AS ENUM ('MICRO_1', 'MICRO_3', 'PROFIT');

-- CreateEnum
CREATE TYPE "PlatitorTVA" AS ENUM ('DA_LUNAR', 'DA_TRIM', 'NU');

-- CreateEnum
CREATE TYPE "Administratie" AS ENUM ('SECTOR_1', 'SECTOR_2', 'SECTOR_3', 'SECTOR_4', 'SECTOR_5', 'SECTOR_6', 'ILFOV', 'BUFTEA', 'BRAGADIRU');

-- CreateTable
CREATE TABLE "Customer" (
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

    CONSTRAINT "Customer_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Detalii" (
    "id" SERIAL NOT NULL,
    "registruUC" BOOLEAN NOT NULL,
    "registruEvFiscala" "DaNuNuECazul" NOT NULL,
    "ofSpalareBani" BOOLEAN NOT NULL,
    "regulamentOrdineInterioara" BOOLEAN NOT NULL,
    "manualPoliticiContabile" BOOLEAN NOT NULL,
    "adresaRevisal" BOOLEAN NOT NULL,
    "parolaITM" TEXT,
    "depunereDeclaratiiOnline" BOOLEAN NOT NULL,
    "accesDosarFiscal" "DaNuNuECazul" NOT NULL,
    "customerId" INTEGER NOT NULL,

    CONSTRAINT "Detalii_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "PunctDeLucru" (
    "id" SERIAL NOT NULL,
    "denumire" TEXT NOT NULL,
    "deLa" TIMESTAMP(3) NOT NULL,
    "panaLa" TIMESTAMP(3),
    "administratie" "Administratie" NOT NULL,
    "registruUC" BOOLEAN NOT NULL,
    "salariati" INTEGER NOT NULL,
    "cui" TEXT,
    "casaDeMarcat" BOOLEAN NOT NULL,
    "customerId" INTEGER NOT NULL,

    CONSTRAINT "PunctDeLucru_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "Istoric" (
    "id" SERIAL NOT NULL,
    "anul" INTEGER NOT NULL,
    "cifraAfaceri" DOUBLE PRECISION NOT NULL,
    "inventar" BOOLEAN NOT NULL,
    "bilantSemIun" "DaNuNuECazul" NOT NULL,
    "bilantAnual" "DaNuNuECazul" NOT NULL,
    "customerId" INTEGER NOT NULL,

    CONSTRAINT "Istoric_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "Detalii_customerId_key" ON "Detalii"("customerId");

-- CreateIndex
CREATE UNIQUE INDEX "PunctDeLucru_customerId_key" ON "PunctDeLucru"("customerId");

-- CreateIndex
CREATE UNIQUE INDEX "Istoric_customerId_key" ON "Istoric"("customerId");

-- AddForeignKey
ALTER TABLE "Detalii" ADD CONSTRAINT "Detalii_customerId_fkey" FOREIGN KEY ("customerId") REFERENCES "Customer"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "PunctDeLucru" ADD CONSTRAINT "PunctDeLucru_customerId_fkey" FOREIGN KEY ("customerId") REFERENCES "Customer"("id") ON DELETE CASCADE ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Istoric" ADD CONSTRAINT "Istoric_customerId_fkey" FOREIGN KEY ("customerId") REFERENCES "Customer"("id") ON DELETE CASCADE ON UPDATE CASCADE;
