"use client";

import React, { useState, useEffect } from 'react';
import { User, Role } from '../../lib/generated/prisma-client';
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";

interface UserFormData {
  email: string;
  name: string;
  rol: Role;
  password?: string;
}

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingUser, setEditingUser] = useState<User | null>(null);
  const [formData, setFormData] = useState<UserFormData>({
    email: '',
    name: '',
    rol: 'USER' as Role,
    password: ''
  });
  const [showReset, setShowReset] = useState<null | number>(null);
  const [resetPassword, setResetPassword] = useState("");
  const [resetLoading, setResetLoading] = useState(false);
  const [saving, setSaving] = useState(false);

  const handleResetPassword = async () => {
    if (!showReset) return;
    setError(null);
    setSuccess(null);
    setResetLoading(true);
    try {
      const res = await fetch(`/api/users/${showReset}/reset-password`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ password: resetPassword }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data?.error || 'Failed to reset password');
      setShowReset(null);
      setResetPassword("");
      setSuccess('Password updated successfully');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      setSuccess(null);
    } finally {
      setResetLoading(false);
    }
  };

  // Fetch users
  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await fetch('/api/users');
      if (!response.ok) {
        throw new Error('Failed to fetch users');
      }
      const data = await response.json();
      setUsers(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
    } finally {
      setLoading(false);
    }
  };

  // Create or update user
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      setSaving(true);
      const url = editingUser ? `/api/users/${editingUser.id}` : '/api/users';
      const method = editingUser ? 'PUT' : 'POST';
      
  const payload: UserFormData = { ...formData };
      if (editingUser) {
        // For edit, if password is empty string, don't send it
        if (!payload.password) delete payload.password;
      }
      const response = await fetch(url, {
        method,
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(payload),
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to save user');
      }

      await fetchUsers();
      setSuccess(editingUser ? 'User updated successfully' : 'User created successfully');
      resetForm();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      setSuccess(null);
    } finally {
      setSaving(false);
    }
  };

  // Delete user
  const handleDelete = async (id: number) => {
    if (!confirm('Are you sure you want to delete this user?')) {
      return;
    }

    try {
      const response = await fetch(`/api/users/${id}`, {
        method: 'DELETE',
      });

      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to delete user');
      }

      await fetchUsers();
      setSuccess('User deleted successfully');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'An error occurred');
      setSuccess(null);
    }
  };

  // Edit user
  const handleEdit = (user: User) => {
    setEditingUser(user);
    setFormData({
      email: user.email,
      name: user.name || '',
      rol: user.rol
    });
    setShowForm(true);
  };

  // Reset form
  const resetForm = () => {
    setFormData({
      email: '',
      name: '',
      rol: 'USER' as Role,
      password: ''
    });
    setEditingUser(null);
    setShowForm(false);
    setError(null);
  };

  useEffect(() => {
    fetchUsers();
  }, []);

  return (
    <div className="container mx-auto px-4 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold text-gray-900">User Management</h1>
        <Button onClick={() => setShowForm(true)}>
          Add User
        </Button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
          {error}
        </div>
      )}
      {success && (
        <div className="bg-green-100 border border-green-400 text-green-800 px-4 py-3 rounded mb-4">
          {success}
        </div>
      )}

      {/* User Form Modal */}
      {showForm && (
        <div
          className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
          onClick={() => resetForm()}
        >
          <div className="bg-white rounded-lg p-6 w-full max-w-md" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold">
                {editingUser ? 'Edit User' : 'Add New User'}
              </h2>
              <Button type="button" variant="ghost" aria-label="Close" onClick={resetForm} className="text-gray-500 hover:text-gray-700">
                ✕
              </Button>
            </div>
            {error && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                {error}
              </div>
            )}
            
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <Label htmlFor="email" className="mb-1">Email *</Label>
                <Input
                  type="email"
                  id="email"
                  value={formData.email}
                  onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                  required
                />
              </div>

              <div>
                <Label htmlFor="name" className="mb-1">Name</Label>
                <Input
                  type="text"
                  id="name"
                  value={formData.name}
                  onChange={(e) => setFormData({ ...formData, name: e.target.value })}
                />
              </div>

              <div>
                <Label htmlFor="password" className="mb-1">Password {editingUser ? '(leave blank to keep current)' : '*'}</Label>
                <Input
                  type="password"
                  id="password"
                  value={formData.password || ''}
                  onChange={(e) => setFormData({ ...formData, password: e.target.value })}
                  minLength={8}
                  required={!editingUser}
                />
              </div>

              <div>
                <Label htmlFor="rol" className="mb-1">Role *</Label>
                <Select value={formData.rol} onValueChange={(v: string) => setFormData({ ...formData, rol: v as Role })}>
                  <SelectTrigger id="rol" aria-label="Select role">
                    <SelectValue placeholder="Select role" />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USER">User</SelectItem>
                    <SelectItem value="ADMIN">Admin</SelectItem>
                    <SelectItem value="MANAGER">Manager</SelectItem>
                  </SelectContent>
                </Select>
              </div>

              <div className="flex gap-3 pt-4">
                <Button type="submit" disabled={saving} className="flex-1">
                  {saving ? 'Saving…' : editingUser ? 'Update' : 'Create'} User
                </Button>
                <Button type="button" variant="outline" onClick={resetForm} className="flex-1">
                  Cancel
                </Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Users Table */}
      {loading ? (
        <div className="p-8 text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-500 mx-auto"></div>
          <p className="mt-2 text-gray-600">Loading users...</p>
        </div>
      ) : users.length === 0 ? (
        <div className="p-8 text-center text-gray-500">
          No users found. Click &quot;Add User&quot; to create your first user.
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Actions</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {users.map((user) => (
                <tr key={user.id} className="hover:bg-gray-50">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{user.id}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{user.email}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">{user.name || '-'}</td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <span
                      className={`inline-flex px-2 py-1 text-xs font-semibold rounded-full ${
                        user.rol === 'ADMIN'
                          ? 'bg-red-100 text-red-800'
                          : user.rol === 'MANAGER'
                          ? 'bg-yellow-100 text-yellow-800'
                          : 'bg-green-100 text-green-800'
                      }`}
                    >
                      {user.rol}
                    </span>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium space-x-2">
                    <button onClick={() => handleEdit(user)} className="text-blue-600 hover:text-blue-900">
                      Edit
                    </button>
                    <button onClick={() => handleDelete(user.id)} className="text-red-600 hover:text-red-900">
                      Delete
                    </button>
                    <button onClick={() => setShowReset(user.id)} className="text-yellow-700 hover:text-yellow-900">
                      Reset Password
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      {/* Reset Password Modal */}
      {showReset && (
        <div
          className="fixed inset-0 bg-black/50 flex items-center justify-center z-50"
          onClick={() => { setShowReset(null); setResetPassword(""); setError(null); }}
        >
          <div className="bg-white rounded-lg p-6 w-full max-w-md" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-xl font-bold">Reset Password</h2>
              <Button type="button" variant="ghost" aria-label="Close" onClick={() => { setShowReset(null); setResetPassword(""); setError(null); }} className="text-gray-500 hover:text-gray-700">
                ✕
              </Button>
            </div>
            {error && (
              <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded mb-4">
                {error}
              </div>
            )}
            <div>
              <Label className="mb-1">New Password</Label>
              <Input
                type="password"
                value={resetPassword}
                onChange={(e) => setResetPassword(e.target.value)}
                minLength={8}
              />
              <p className="text-xs text-gray-500 mt-1">Must be at least 8 characters, include upper/lowercase, a number and a symbol.</p>
            </div>
            <div className="flex gap-3 pt-4">
              <Button onClick={handleResetPassword} disabled={resetLoading} className="flex-1">
                {resetLoading ? 'Updating...' : 'Update Password'}
              </Button>
              <Button onClick={() => { setShowReset(null); setResetPassword(''); }} variant="outline" className="flex-1">
                Cancel
              </Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;