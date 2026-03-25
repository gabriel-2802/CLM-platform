-- CreateEnum
CREATE TYPE "ConditionOperator" AS ENUM ('EQUALS', 'NOT_EQUALS', 'IS_TRUE', 'IS_FALSE', 'IN');

-- CreateEnum
CREATE TYPE "Frequency" AS ENUM ('MONTHLY', 'QUARTERLY', 'YEARLY');

-- CreateTable
CREATE TABLE "Rule" (
    "id" SERIAL NOT NULL,
    "name" TEXT NOT NULL,
    "description" TEXT,
    "frequency" "Frequency" NOT NULL,
    "taskTitle" TEXT NOT NULL,
    "taskNotes" TEXT,
    "active" BOOLEAN NOT NULL DEFAULT true,
    "createdAt" TIMESTAMP(3) NOT NULL DEFAULT CURRENT_TIMESTAMP,
    "updatedAt" TIMESTAMP(3) NOT NULL,

    CONSTRAINT "Rule_pkey" PRIMARY KEY ("id")
);

-- CreateTable
CREATE TABLE "RuleCondition" (
    "id" SERIAL NOT NULL,
    "field" TEXT NOT NULL,
    "operator" "ConditionOperator" NOT NULL DEFAULT 'EQUALS',
    "value" TEXT NOT NULL,
    "ruleId" INTEGER NOT NULL,

    CONSTRAINT "RuleCondition_pkey" PRIMARY KEY ("id")
);

-- AddForeignKey
ALTER TABLE "RuleCondition" ADD CONSTRAINT "RuleCondition_ruleId_fkey" FOREIGN KEY ("ruleId") REFERENCES "Rule"("id") ON DELETE CASCADE ON UPDATE CASCADE;
