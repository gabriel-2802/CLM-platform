"use client";

import React, { useState, useEffect } from 'react';
import { Input } from "@/components/ui/input";
import { Button } from "@/components/ui/button";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { API } from "@/lib/endpoints";

type AppRole = 'USER' | 'ADMIN' | 'MANAGER';

interface AppUser {
  id: number;
  email: string;
  name: string | null;
  enabled: boolean;
  rol: AppRole;
}

interface UserFormData {
  email: string;
  name: string;
  rol: AppRole;
  password?: string;
}

const ROLE_BADGE: Record<AppRole, string> = {
  ADMIN: "bg-red-100 text-red-800",
  MANAGER: "bg-amber-100 text-amber-800",
  USER: "bg-green-100 text-green-800",
};

const UserManagement: React.FC = () => {
  const [users, setUsers] = useState<AppUser[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [showForm, setShowForm] = useState(false);
  const [editingUser, setEditingUser] = useState<AppUser | null>(null);
  const [formData, setFormData] = useState<UserFormData>({ email: '', name: '', rol: 'USER', password: '' });
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
      const res = await fetch(API.users.resetPassword(showReset), {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ password: resetPassword }),
      });
      const data = await res.json();
      if (!res.ok) throw new Error(data?.error || 'Failed to reset password');
      setShowReset(null);
      setResetPassword("");
      setSuccess('Parolă actualizată cu succes');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'A apărut o eroare');
      setSuccess(null);
    } finally {
      setResetLoading(false);
    }
  };

  const fetchUsers = async () => {
    try {
      setLoading(true);
      const response = await fetch(API.users.list, { cache: 'no-store' });
      if (!response.ok) throw new Error('Failed to fetch users');
      setUsers(await response.json());
    } catch (err) {
      setError(err instanceof Error ? err.message : 'A apărut o eroare');
    } finally {
      setLoading(false);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);
    try {
      setSaving(true);
      const url = editingUser ? API.users.byId(editingUser.id) : API.users.list;
      const method = editingUser ? 'PUT' : 'POST';
      const payload: UserFormData = { ...formData };
      if (editingUser && !payload.password) delete payload.password;
      const response = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(payload) });
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to save user');
      }
      await fetchUsers();
      setSuccess(editingUser ? 'Utilizator actualizat' : 'Utilizator creat');
      resetForm();
    } catch (err) {
      setError(err instanceof Error ? err.message : 'A apărut o eroare');
      setSuccess(null);
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id: number) => {
    if (!confirm('Ești sigur că vrei să ștergi acest utilizator?')) return;
    try {
      const response = await fetch(API.users.byId(id), { method: 'DELETE' });
      if (!response.ok) {
        const errorData = await response.json();
        throw new Error(errorData.error || 'Failed to delete user');
      }
      await fetchUsers();
      setSuccess('Utilizator șters');
    } catch (err) {
      setError(err instanceof Error ? err.message : 'A apărut o eroare');
      setSuccess(null);
    }
  };

  const handleEdit = (user: AppUser) => {
    setEditingUser(user);
    setFormData({ email: user.email, name: user.name || '', rol: user.rol });
    setShowForm(true);
  };

  const resetForm = () => {
    setFormData({ email: '', name: '', rol: 'USER', password: '' });
    setEditingUser(null);
    setShowForm(false);
    setError(null);
  };

  useEffect(() => { fetchUsers(); }, []);

  return (
    <div className="p-6 space-y-4 w-full">
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-xl font-semibold text-slate-800">Gestionare utilizatori</h1>
          <p className="text-sm text-muted-foreground mt-0.5">Administrează conturile de acces</p>
        </div>
        <Button size="sm" onClick={() => setShowForm(true)}>+ Utilizator nou</Button>
      </div>

      {error && <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-2 rounded text-sm">{error}</div>}
      {success && <div className="bg-green-50 border border-green-200 text-green-800 px-4 py-2 rounded text-sm">{success}</div>}

      {loading ? (
        <div className="p-8 text-center text-muted-foreground text-sm">Se încarcă...</div>
      ) : users.length === 0 ? (
        <div className="p-8 text-center text-muted-foreground text-sm">Niciun utilizator găsit.</div>
      ) : (
        <div className="overflow-hidden rounded-lg border border-slate-200 shadow-sm">
          <div className="overflow-x-auto">
            <table className="min-w-full text-xs md:text-sm">
              <thead>
                <tr className="bg-slate-700">
                  <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">ID</th>
                  <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Email</th>
                  <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Nume</th>
                  <th className="px-4 py-2.5 text-left text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Rol</th>
                  <th className="px-4 py-2.5 text-right text-xs font-semibold text-slate-100 uppercase tracking-wide whitespace-nowrap">Acțiuni</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-slate-100">
                {users.map((user) => (
                  <tr key={user.id} className="hover:bg-slate-50 transition-colors">
                    <td className="px-4 py-2 whitespace-nowrap text-slate-500">{user.id}</td>
                    <td className="px-4 py-2 whitespace-nowrap text-slate-800">{user.email}</td>
                    <td className="px-4 py-2 whitespace-nowrap font-medium text-slate-800">{user.name || '—'}</td>
                    <td className="px-4 py-2 whitespace-nowrap">
                      <span className={`inline-flex px-2 py-0.5 text-xs font-semibold rounded-full ${ROLE_BADGE[user.rol] ?? "bg-slate-100 text-slate-600"}`}>
                        {user.rol}
                      </span>
                    </td>
                    <td className="px-4 py-2 whitespace-nowrap text-right space-x-3">
                      <button onClick={() => handleEdit(user)} className="text-slate-600 hover:text-slate-900 transition-colors">Editează</button>
                      <button onClick={() => handleDelete(user.id)} className="text-red-600 hover:text-red-900 transition-colors">Șterge</button>
                      <button onClick={() => setShowReset(user.id)} className="text-amber-700 hover:text-amber-900 transition-colors">Resetează parolă</button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {showForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center backdrop-blur-[2px] animate-in fade-in-0 duration-500" onClick={() => resetForm()}>
          <div className="bg-white rounded-lg border border-slate-200 shadow-lg p-6 w-full max-w-md" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-slate-800">{editingUser ? 'Editează utilizator' : 'Utilizator nou'}</h2>
              <Button type="button" variant="ghost" size="sm" onClick={resetForm}>✕</Button>
            </div>
            {error && <div className="bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded text-sm mb-4">{error}</div>}
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <Label htmlFor="email" className="mb-1">Email *</Label>
                <Input type="email" id="email" value={formData.email} onChange={(e) => setFormData({ ...formData, email: e.target.value })} required />
              </div>
              <div>
                <Label htmlFor="name" className="mb-1">Nume</Label>
                <Input type="text" id="name" value={formData.name} onChange={(e) => setFormData({ ...formData, name: e.target.value })} />
              </div>
              <div>
                <Label htmlFor="password" className="mb-1">
                  Parolă {editingUser ? '(lasă gol pentru a păstra)' : '*'}
                </Label>
                <Input type="password" id="password" value={formData.password || ''} onChange={(e) => setFormData({ ...formData, password: e.target.value })} minLength={8} required={!editingUser} />
              </div>
              <div>
                <Label htmlFor="rol" className="mb-1">Rol *</Label>
                <Select value={formData.rol} onValueChange={(v: string) => setFormData({ ...formData, rol: v as AppRole })}>
                  <SelectTrigger id="rol"><SelectValue placeholder="Alege rol" /></SelectTrigger>
                  <SelectContent>
                    <SelectItem value="USER">User</SelectItem>
                    <SelectItem value="ADMIN">Admin</SelectItem>
                    <SelectItem value="MANAGER">Manager</SelectItem>
                  </SelectContent>
                </Select>
              </div>
              <div className="flex gap-3 pt-2">
                <Button type="submit" disabled={saving} className="flex-1">{saving ? 'Se salvează…' : editingUser ? 'Actualizează' : 'Creează'}</Button>
                <Button type="button" variant="outline" onClick={resetForm} className="flex-1">Anulează</Button>
              </div>
            </form>
          </div>
        </div>
      )}

      {showReset && (
        <div className="fixed inset-0 z-50 flex items-center justify-center backdrop-blur-[2px] animate-in fade-in-0 duration-500" onClick={() => { setShowReset(null); setResetPassword(""); setError(null); }}>
          <div className="bg-white rounded-lg border border-slate-200 shadow-lg p-6 w-full max-w-md" onClick={(e) => e.stopPropagation()}>
            <div className="flex items-center justify-between mb-4">
              <h2 className="text-lg font-semibold text-slate-800">Resetează parolă</h2>
              <Button type="button" variant="ghost" size="sm" onClick={() => { setShowReset(null); setResetPassword(""); setError(null); }}>✕</Button>
            </div>
            {error && <div className="bg-red-50 border border-red-200 text-red-700 px-3 py-2 rounded text-sm mb-4">{error}</div>}
            <div className="space-y-3">
              <div>
                <Label className="mb-1">Parolă nouă</Label>
                <Input type="password" value={resetPassword} onChange={(e) => setResetPassword(e.target.value)} minLength={8} />
                <p className="text-xs text-muted-foreground mt-1">Minim 8 caractere, cu majuscule, cifre și simboluri.</p>
              </div>
              <div className="flex gap-3 pt-2">
                <Button onClick={handleResetPassword} disabled={resetLoading} className="flex-1">{resetLoading ? 'Se actualizează...' : 'Actualizează parola'}</Button>
                <Button onClick={() => { setShowReset(null); setResetPassword(''); }} variant="outline" className="flex-1">Anulează</Button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagement;
