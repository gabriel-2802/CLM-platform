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
    
    // Create date for the first day of the specified month and year (in UTC)
    const taskDate = new Date(Date.UTC(yearNum, monthNum - 1, 1));
    
    // Fetch all clients
    const clients = await prisma.client.findMany();
    const tasks = [];

    for (const client of clients) {
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
        const taskTitles = ['Avem acte', 'Introdus acte', 'Verificat acte', 'Luna printata'];
        
        for (const title of taskTitles) {
          const task = await prisma.task.create({
            data: {
              title,
              notes: null,
              date: taskDate,
              clientId: client.id,
              userId: assignedUser.id,
            },
          });
          tasks.push(task);
        }
      }
    }

    return NextResponse.json({ message: 'Tasks generated successfully', tasks });
  } catch (error) {
    console.error('Error generating tasks:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}