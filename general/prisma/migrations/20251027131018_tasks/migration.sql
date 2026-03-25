-- CreateTable
CREATE TABLE "Task" (
    "id" SERIAL NOT NULL,
    "done" BOOLEAN NOT NULL DEFAULT false,
    "title" TEXT NOT NULL,
    "notes" TEXT,
    "blocked" TEXT,
    "objective" TEXT,
    "date" TIMESTAMP(3) NOT NULL,
    "userId" INTEGER NOT NULL,
    "customerId" INTEGER NOT NULL,

    CONSTRAINT "Task_pkey" PRIMARY KEY ("id")
);

-- CreateIndex
CREATE UNIQUE INDEX "Task_userId_key" ON "Task"("userId");

-- CreateIndex
CREATE UNIQUE INDEX "Task_customerId_key" ON "Task"("customerId");

-- AddForeignKey
ALTER TABLE "Task" ADD CONSTRAINT "Task_userId_fkey" FOREIGN KEY ("userId") REFERENCES "User"("id") ON DELETE RESTRICT ON UPDATE CASCADE;

-- AddForeignKey
ALTER TABLE "Task" ADD CONSTRAINT "Task_customerId_fkey" FOREIGN KEY ("customerId") REFERENCES "Customer"("id") ON DELETE CASCADE ON UPDATE CASCADE;
