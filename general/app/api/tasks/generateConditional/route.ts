import { PrismaClient } from '@/lib/generated/prisma-client';
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

    // Parse and validate request parameters
    const { searchParams } = new URL(request.url);
    const clientId = searchParams.get('clientId');
    const month = searchParams.get('month');
    const year = searchParams.get('year');
    const note = searchParams.get('note') || '390'; // Default to "390"
    
    if (!clientId || !month || !year) {
      return NextResponse.json({ error: 'Missing clientId, month, or year parameter' }, { status: 400 });
    }
    
    const clientIdNum = parseInt(clientId);
    const monthNum = parseInt(month);
    const yearNum = parseInt(year);
    
    if (isNaN(clientIdNum)) {
      return NextResponse.json({ error: 'Invalid clientId' }, { status: 400 });
    }
    
    if (isNaN(monthNum) || monthNum < 1 || monthNum > 12) {
      return NextResponse.json({ error: 'Invalid month. Must be between 1 and 12' }, { status: 400 });
    }
    
    if (isNaN(yearNum) || yearNum < 1900 || yearNum > 2100) {
      return NextResponse.json({ error: 'Invalid year' }, { status: 400 });
    }
    
    // Verify client exists
    const client = await prisma.client.findUnique({
      where: { id: clientIdNum },
    });
    
    if (!client) {
      return NextResponse.json({ error: `Client with id ${clientIdNum} not found` }, { status: 404 });
    }
    
    // Create date for the 25th day of the specified month and year (in UTC)
    const taskDate = new Date(Date.UTC(yearNum, monthNum - 1, 25));
    
    // Get all users assigned to this client
    const userClients = await prisma.userClient.findMany({
      where: {
        clientId: clientIdNum,
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
    
    if (!assignedUser) {
      return NextResponse.json({ error: 'No USER or MANAGER assigned to this client' }, { status: 400 });
    }
    
    const results = [];
    const taskTitles = ['Generat declaratii', 'Depus declaratii'];
    
    // Process both task types
    for (const title of taskTitles) {
      // Find existing task for this client, month, year, and title
      const existingTask = await prisma.task.findFirst({
        where: {
          clientId: clientIdNum,
          title,
          date: taskDate,
        },
      });
      
      if (existingTask) {
        // Task exists - check if note is already in notes
        const currentNotes = existingTask.notes || '';
        const notesList = currentNotes.split(',').map(n => n.trim()).filter(n => n);
        
        if (!notesList.includes(note)) {
          // Add the note to the list
          notesList.push(note);
          const updatedNotes = notesList.join(',');
          
          // Update the task
          const updatedTask = await prisma.task.update({
            where: { id: existingTask.id },
            data: { notes: updatedNotes },
          });
          
          results.push({
            action: 'updated',
            title,
            taskId: updatedTask.id,
            notes: updatedNotes,
          });
        } else {
          results.push({
            action: 'skipped',
            title,
            taskId: existingTask.id,
            notes: currentNotes,
            reason: `Note "${note}" already exists`,
          });
        }
      } else {
        // Task doesn't exist - create new one
        const newTask = await prisma.task.create({
          data: {
            title,
            notes: note,
            date: taskDate,
            clientId: clientIdNum,
            userId: assignedUser.id,
          },
        });
        
        results.push({
          action: 'created',
          title,
          taskId: newTask.id,
          notes: note,
        });
      }
    }
    
    return NextResponse.json({ 
      message: 'Tasks processed successfully',
      clientId: clientIdNum,
      month: monthNum,
      year: yearNum,
      note,
      results,
    });
    
  } catch (error) {
    console.error('Error processing tasks:', error);
    return NextResponse.json({ error: 'Internal Server Error' }, { status: 500 });
  }
}
