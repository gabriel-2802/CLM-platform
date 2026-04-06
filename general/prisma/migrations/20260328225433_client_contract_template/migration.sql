-- AlterTable
ALTER TABLE "Client" ADD COLUMN     "contractGenTemplateId" INTEGER;

-- AddForeignKey
ALTER TABLE "Client" ADD CONSTRAINT "Client_contractGenTemplateId_fkey" FOREIGN KEY ("contractGenTemplateId") REFERENCES "ContractTemplate"("id") ON DELETE SET NULL ON UPDATE CASCADE;
