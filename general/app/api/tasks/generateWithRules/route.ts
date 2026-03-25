import { PrismaClient, Frequency, Client, RuleCondition } from '@/lib/generated/prisma-client';
import { NextResponse } from 'next/server';
import bcrypt from 'bcryptjs';

const prisma = new PrismaClient();

async function checkBasicAuth(request: Request): Promise<boolean> {
  const authHeader = request.headers.get('authorization');
  
  if (!authHeader || !authHeader.startsWith('Basic ')) {
    return false;
  }

  const base64Credentials = authHeader.slice(6);
  const credentials = Buffer.from(base64Credentials, 'base64').toString('utf-8');
  const [email, password] = credentials.split(':');

  // Find user by email with ADMIN role
  const user = await prisma.user.findUnique({
    where: { email },
  });

  if (!user || user.rol !== 'ADMIN' || !user.password) {
    return false;
  }

  // Verify password
  const isValidPassword = await bcrypt.compare(password, user.password);
  return isValidPassword;
}

function matchesAllConditions(client: Client, conditions: RuleCondition[]): boolean {
  return conditions.every((c) => {
    switch (c.operator) {
      case 'EQUALS':
        return (client as unknown as Record<string, unknown>)[c.field] === c.value;
      case 'IN':
        return c.value.split(',').includes(String((client as unknown as Record<string, unknown>)[c.field]));
      case 'IS_TRUE':
        return Boolean((client as unknown as Record<string, unknown>)[c.field]);
      default:
        return false;
    }
  });
}

export async function POST(request: Request) {
  try {
    // Check Basic Auth
    const isAuthenticated = await checkBasicAuth(request);
    
    if (!isAuthenticated) {
      return NextResponse.json(
        { error: 'Unauthorized - Admin credentials required' },
        { 
          status: 401,
          headers: { 'WWW-Authenticate': 'Basic realm="Task Generation API"' }
        }
      );
    }

    // Parse and validate month and year from URL query parameters
    const { searchParams } = new URL(request.url);
    const month = searchParams.get('month');
    const year = searchParams.get('year');
    
    if (!month || !year) {
      return NextResponse.json({ error: 'Missing month or year parameter' }, { status: 400 });
    }
    
    const monthNum = parseInt(month);
    const yearNum = parseInt(year);
    
    if (isNaN(monthNum) || monthNum < 1 || monthNum > 12) {
      return NextResponse.json({ error: 'Invalid month. Must be between 1 and 12' }, { status: 400 });
    }
    
    if (isNaN(yearNum) || yearNum < 1900 || yearNum > 2100) {
      return NextResponse.json({ error: 'Invalid year' }, { status: 400 });
    }
    
    // Create date for the 25th day of the specified month and year (in UTC)
    const taskDate = new Date(Date.UTC(yearNum, monthNum - 1, 25));
    
    // Fetch all active rules
    const rules = await prisma.rule.findMany({
      where: {
        active: true,
      },
      include: {
        conditions: true,
      },
    });

    if (rules.length === 0) {
      return NextResponse.json({ message: 'No active rules found.' });
    }

    // Quarterly months: March (3), June (6), September (9), December (12)
    const isQuarterlyMonth = [3, 6, 9, 12].includes(monthNum);

    // Fetch all clients
    const clients = await prisma.client.findMany();
    const tasks = [];

    // For each client
    for (const client of clients) {
      const collectedTitles: string[] = [];

      // Go through all rules
      for (const rule of rules) {
        // Check if rule matches all conditions
        if (matchesAllConditions(client, rule.conditions)) {
          // Check frequency conditions
          const shouldInclude = 
            rule.frequency === 'MONTHLY' || 
            (rule.frequency === 'QUARTERLY' && isQuarterlyMonth);

          if (shouldInclude) {
            // Collect task title
            collectedTitles.push(rule.taskTitle);
          }
        }
      }

      // If we have collected titles, create a task
      if (collectedTitles.length > 0) {
        // Join titles with comma, split to remove duplicates, then join again
        const joinedTitles = collectedTitles.join(',');
        const uniqueTitles = [...new Set(joinedTitles.split(','))];
        const finalNotes = uniqueTitles.join(',');
        
        // Get all users assigned to this client through UserClient table
        const userClients = await prisma.userClient.findMany({
          where: {
            clientId: client.id,
          },
          include: {
            user: true,
          },
        });

        // Find a USER role user first, if not found, then find a MANAGER role user
        let assignedUser = userClients.find(uc => uc.user.rol === 'USER')?.user;
        
        if (!assignedUser) {
          assignedUser = userClients.find(uc => uc.user.rol === 'MANAGER')?.user;
        }

        // Only create tasks if a user was found
        if (assignedUser) {
          // Create "Generat declaratii" task
          const task1 = await prisma.task.create({
            data: {
              title: 'Generat declaratii',
              notes: finalNotes,
              date: taskDate,
              clientId: client.id,
              userId: assignedUser.id,
            },
          });
          tasks.push(task1);

          // Create "Depus declaratii" task
          const task2 = await prisma.task.create({
            data: {
              title: 'Depus declaratii',
              notes: finalNotes,
              date: taskDate,
              clientId: client.id,
              userId: assignedUser.id,
            },
          });
          tasks.push(task2);
        }
      }
    }

    return NextResponse.json({ message: 'Tasks generated successfully', tasks });
  } catch (error) {
    console.error('Error generating tasks:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
