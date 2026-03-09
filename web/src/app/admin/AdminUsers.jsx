import { useState, useMemo } from "react";
import { persons, accounts } from "../../data/mockData";

/* ——————————————————————————————————————————————
   Admin Users — CRUD table with dark theme
   Background: #0A0F1A  Accent: #00E5A0
   —————————————————————————————————————————————— */

const ROLES = ["all", "admin", "commander", "physician", "psychologist", "trainer", "soldier", "patient"];
const PLANS = ["all", "Basic", "Pro", "Enterprise"];
const PAGE_SIZE = 8;

function buildUserList() {
  return persons.map((p, i) => {
    const acct = accounts.find((a) => a.personId === p.id);
    return {
      id: p.id,
      name: p.name,
      email: acct ? acct.email : `${p.name.toLowerCase().replace(/\s+/g, ".")}@vitanova.mil`,
      role: p.role,
      plan: i % 3 === 0 ? "Enterprise" : i % 3 === 1 ? "Pro" : "Basic",
      status: p.status === "critical" ? "Inactive" : "Active",
      createdDate: new Date(Date.now() - (i * 7 + 10) * 86400000).toISOString().split("T")[0],
      avatar: p.avatar,
    };
  });
}

/* ——— Modal Component ——— */
function Modal({ open, onClose, title, children }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60" onClick={onClose} />
      <div className="relative bg-[#111827] border border-gray-700 rounded-2xl shadow-2xl w-full max-w-lg p-6 z-10">
        <div className="flex items-center justify-between mb-5">
          <h3 className="text-lg font-bold text-white">{title}</h3>
          <button onClick={onClose} className="text-gray-400 hover:text-white transition-colors text-xl leading-none">&times;</button>
        </div>
        {children}
      </div>
    </div>
  );
}

/* ——— Confirm Dialog ——— */
function ConfirmDialog({ open, onClose, onConfirm, message }) {
  if (!open) return null;
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-black/60" onClick={onClose} />
      <div className="relative bg-[#111827] border border-gray-700 rounded-2xl shadow-2xl w-full max-w-sm p-6 z-10">
        <p className="text-gray-200 mb-6">{message}</p>
        <div className="flex justify-end gap-3">
          <button onClick={onClose} className="px-4 py-2 text-sm text-gray-300 bg-gray-800 rounded-lg hover:bg-gray-700 transition-colors">Cancel</button>
          <button onClick={onConfirm} className="px-4 py-2 text-sm text-[#0A0F1A] bg-red-500 rounded-lg hover:bg-red-400 transition-colors font-semibold">Delete</button>
        </div>
      </div>
    </div>
  );
}

export default function AdminUsers() {
  const [users, setUsers] = useState(() => buildUserList());
  const [search, setSearch] = useState("");
  const [roleFilter, setRoleFilter] = useState("all");
  const [planFilter, setPlanFilter] = useState("all");
  const [page, setPage] = useState(1);

  /* Modal states */
  const [showAddModal, setShowAddModal] = useState(false);
  const [editUser, setEditUser] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);

  /* Form state */
  const [formName, setFormName] = useState("");
  const [formEmail, setFormEmail] = useState("");
  const [formRole, setFormRole] = useState("soldier");
  const [formPlan, setFormPlan] = useState("Basic");

  /* Filtering */
  const filtered = useMemo(() => {
    let list = users;
    if (search.trim()) {
      const q = search.toLowerCase();
      list = list.filter(
        (u) =>
          u.name.toLowerCase().includes(q) ||
          u.email.toLowerCase().includes(q)
      );
    }
    if (roleFilter !== "all") list = list.filter((u) => u.role === roleFilter);
    if (planFilter !== "all") list = list.filter((u) => u.plan === planFilter);
    return list;
  }, [users, search, roleFilter, planFilter]);

  const totalPages = Math.max(1, Math.ceil(filtered.length / PAGE_SIZE));
  const pageData = filtered.slice((page - 1) * PAGE_SIZE, page * PAGE_SIZE);

  /* Handlers */
  function openAdd() {
    setFormName("");
    setFormEmail("");
    setFormRole("soldier");
    setFormPlan("Basic");
    setShowAddModal(true);
  }

  function handleAdd() {
    if (!formName.trim() || !formEmail.trim()) return;
    const newUser = {
      id: `person-new-${Date.now()}`,
      name: formName.trim(),
      email: formEmail.trim(),
      role: formRole,
      plan: formPlan,
      status: "Active",
      createdDate: new Date().toISOString().split("T")[0],
      avatar: `https://api.dicebear.com/9.x/notionists/svg?seed=${encodeURIComponent(formName)}`,
    };
    setUsers((prev) => [newUser, ...prev]);
    setShowAddModal(false);
    setPage(1);
  }

  function openEdit(user) {
    setFormName(user.name);
    setFormEmail(user.email);
    setFormRole(user.role);
    setFormPlan(user.plan);
    setEditUser(user);
  }

  function handleEdit() {
    if (!editUser) return;
    setUsers((prev) =>
      prev.map((u) =>
        u.id === editUser.id
          ? { ...u, name: formName, email: formEmail, role: formRole, plan: formPlan }
          : u
      )
    );
    setEditUser(null);
  }

  function handleDeactivate(id) {
    setUsers((prev) =>
      prev.map((u) =>
        u.id === id ? { ...u, status: u.status === "Active" ? "Inactive" : "Active" } : u
      )
    );
  }

  function handleDelete() {
    if (!deleteTarget) return;
    setUsers((prev) => prev.filter((u) => u.id !== deleteTarget));
    setDeleteTarget(null);
  }

  function handleExportCSV() {
    const header = "Name,Email,Role,Plan,Status,Created\n";
    const rows = filtered.map((u) => `${u.name},${u.email},${u.role},${u.plan},${u.status},${u.createdDate}`).join("\n");
    const blob = new Blob([header + rows], { type: "text/csv" });
    const url = URL.createObjectURL(blob);
    const a = document.createElement("a");
    a.href = url;
    a.download = "vitanova_users.csv";
    a.click();
    URL.revokeObjectURL(url);
  }

  /* Shared form fields */
  function renderForm() {
    return (
      <div className="space-y-4">
        <div>
          <label className="block text-xs text-gray-400 mb-1 uppercase tracking-wide">Name</label>
          <input value={formName} onChange={(e) => setFormName(e.target.value)} className="w-full bg-[#0A0F1A] border border-gray-600 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-[#00E5A0]" />
        </div>
        <div>
          <label className="block text-xs text-gray-400 mb-1 uppercase tracking-wide">Email</label>
          <input value={formEmail} onChange={(e) => setFormEmail(e.target.value)} className="w-full bg-[#0A0F1A] border border-gray-600 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-[#00E5A0]" />
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div>
            <label className="block text-xs text-gray-400 mb-1 uppercase tracking-wide">Role</label>
            <select value={formRole} onChange={(e) => setFormRole(e.target.value)} className="w-full bg-[#0A0F1A] border border-gray-600 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-[#00E5A0]">
              {ROLES.filter((r) => r !== "all").map((r) => (
                <option key={r} value={r}>{r}</option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs text-gray-400 mb-1 uppercase tracking-wide">Plan</label>
            <select value={formPlan} onChange={(e) => setFormPlan(e.target.value)} className="w-full bg-[#0A0F1A] border border-gray-600 rounded-lg px-3 py-2 text-sm text-white focus:outline-none focus:ring-2 focus:ring-[#00E5A0]">
              {PLANS.filter((p) => p !== "all").map((p) => (
                <option key={p} value={p}>{p}</option>
              ))}
            </select>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-[#0A0F1A] text-white p-6">
      {/* Header */}
      <div className="flex items-center justify-between mb-6">
        <div>
          <h1 className="text-2xl font-bold">User Management</h1>
          <p className="text-gray-400 text-sm mt-1">{filtered.length} users total</p>
        </div>
        <div className="flex items-center gap-3">
          <button onClick={handleExportCSV} className="px-4 py-2 text-sm bg-gray-800 border border-gray-700 rounded-lg hover:bg-gray-700 transition-colors text-gray-300">
            <span className="mr-2">&#8615;</span>Export CSV
          </button>
          <button onClick={openAdd} className="px-4 py-2 text-sm bg-[#00E5A0] text-[#0A0F1A] rounded-lg hover:bg-[#00cc8e] transition-colors font-semibold">
            + Add User
          </button>
        </div>
      </div>

      {/* Filters */}
      <div className="flex items-center gap-4 mb-6">
        <div className="relative flex-1 max-w-sm">
          <svg className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
          <input
            type="text"
            placeholder="Search by name or email..."
            value={search}
            onChange={(e) => { setSearch(e.target.value); setPage(1); }}
            className="w-full pl-10 pr-4 py-2.5 bg-[#111827] border border-gray-700 rounded-lg text-sm text-white placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-[#00E5A0] focus:border-transparent"
          />
        </div>
        <select
          value={roleFilter}
          onChange={(e) => { setRoleFilter(e.target.value); setPage(1); }}
          className="bg-[#111827] border border-gray-700 rounded-lg px-3 py-2.5 text-sm text-gray-300 focus:outline-none focus:ring-2 focus:ring-[#00E5A0]"
        >
          {ROLES.map((r) => (
            <option key={r} value={r}>{r === "all" ? "All Roles" : r}</option>
          ))}
        </select>
        <select
          value={planFilter}
          onChange={(e) => { setPlanFilter(e.target.value); setPage(1); }}
          className="bg-[#111827] border border-gray-700 rounded-lg px-3 py-2.5 text-sm text-gray-300 focus:outline-none focus:ring-2 focus:ring-[#00E5A0]"
        >
          {PLANS.map((p) => (
            <option key={p} value={p}>{p === "all" ? "All Plans" : p}</option>
          ))}
        </select>
      </div>

      {/* Table */}
      <div className="bg-[#111827] border border-gray-800 rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-gray-800 text-gray-400 text-xs uppercase tracking-wider">
                <th className="text-left px-5 py-3 font-medium">Name</th>
                <th className="text-left px-5 py-3 font-medium">Email</th>
                <th className="text-left px-5 py-3 font-medium">Role</th>
                <th className="text-left px-5 py-3 font-medium">Plan</th>
                <th className="text-left px-5 py-3 font-medium">Status</th>
                <th className="text-left px-5 py-3 font-medium">Created</th>
                <th className="text-right px-5 py-3 font-medium">Actions</th>
              </tr>
            </thead>
            <tbody>
              {pageData.length === 0 ? (
                <tr>
                  <td colSpan={7} className="text-center py-12 text-gray-500">No users found</td>
                </tr>
              ) : (
                pageData.map((user) => (
                  <tr key={user.id} className="border-b border-gray-800/50 hover:bg-[#0A0F1A]/50 transition-colors">
                    <td className="px-5 py-3">
                      <div className="flex items-center gap-3">
                        <img src={user.avatar} alt="" className="w-8 h-8 rounded-full bg-gray-700 flex-shrink-0" />
                        <span className="font-medium text-white">{user.name}</span>
                      </div>
                    </td>
                    <td className="px-5 py-3 text-gray-400">{user.email}</td>
                    <td className="px-5 py-3">
                      <span className="inline-block px-2 py-0.5 rounded-md bg-[#0A0F1A] text-[#00E5A0] text-xs font-medium border border-[#00E5A0]/30">
                        {user.role}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-gray-300">{user.plan}</td>
                    <td className="px-5 py-3">
                      <span className={`inline-flex items-center gap-1.5 text-xs font-medium ${user.status === "Active" ? "text-[#00E5A0]" : "text-red-400"}`}>
                        <span className={`w-1.5 h-1.5 rounded-full ${user.status === "Active" ? "bg-[#00E5A0]" : "bg-red-400"}`} />
                        {user.status}
                      </span>
                    </td>
                    <td className="px-5 py-3 text-gray-500">{user.createdDate}</td>
                    <td className="px-5 py-3 text-right">
                      <div className="flex items-center justify-end gap-2">
                        <button onClick={() => openEdit(user)} className="px-2.5 py-1 text-xs bg-gray-800 border border-gray-700 rounded-md hover:bg-gray-700 transition-colors text-gray-300" title="Edit">Edit</button>
                        <button onClick={() => handleDeactivate(user.id)} className="px-2.5 py-1 text-xs bg-gray-800 border border-gray-700 rounded-md hover:bg-gray-700 transition-colors text-amber-400" title="Toggle status">
                          {user.status === "Active" ? "Deactivate" : "Activate"}
                        </button>
                        <button onClick={() => setDeleteTarget(user.id)} className="px-2.5 py-1 text-xs bg-gray-800 border border-gray-700 rounded-md hover:bg-red-900/50 hover:border-red-700 transition-colors text-red-400" title="Delete">Delete</button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        <div className="flex items-center justify-between px-5 py-3 border-t border-gray-800">
          <span className="text-xs text-gray-500">
            Showing {(page - 1) * PAGE_SIZE + 1}–{Math.min(page * PAGE_SIZE, filtered.length)} of {filtered.length}
          </span>
          <div className="flex items-center gap-1">
            <button
              onClick={() => setPage((p) => Math.max(1, p - 1))}
              disabled={page === 1}
              className="px-3 py-1.5 text-xs bg-gray-800 border border-gray-700 rounded-md hover:bg-gray-700 transition-colors text-gray-300 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Prev
            </button>
            {Array.from({ length: totalPages }, (_, i) => i + 1).map((p) => (
              <button
                key={p}
                onClick={() => setPage(p)}
                className={`px-3 py-1.5 text-xs rounded-md border transition-colors ${
                  p === page
                    ? "bg-[#00E5A0] text-[#0A0F1A] border-[#00E5A0] font-semibold"
                    : "bg-gray-800 border-gray-700 text-gray-300 hover:bg-gray-700"
                }`}
              >
                {p}
              </button>
            ))}
            <button
              onClick={() => setPage((p) => Math.min(totalPages, p + 1))}
              disabled={page === totalPages}
              className="px-3 py-1.5 text-xs bg-gray-800 border border-gray-700 rounded-md hover:bg-gray-700 transition-colors text-gray-300 disabled:opacity-40 disabled:cursor-not-allowed"
            >
              Next
            </button>
          </div>
        </div>
      </div>

      {/* Add User Modal */}
      <Modal open={showAddModal} onClose={() => setShowAddModal(false)} title="Add New User">
        {renderForm()}
        <div className="flex justify-end gap-3 mt-6">
          <button onClick={() => setShowAddModal(false)} className="px-4 py-2 text-sm text-gray-300 bg-gray-800 rounded-lg hover:bg-gray-700 transition-colors">Cancel</button>
          <button onClick={handleAdd} className="px-4 py-2 text-sm text-[#0A0F1A] bg-[#00E5A0] rounded-lg hover:bg-[#00cc8e] transition-colors font-semibold">Create User</button>
        </div>
      </Modal>

      {/* Edit User Modal */}
      <Modal open={!!editUser} onClose={() => setEditUser(null)} title="Edit User">
        {renderForm()}
        <div className="flex justify-end gap-3 mt-6">
          <button onClick={() => setEditUser(null)} className="px-4 py-2 text-sm text-gray-300 bg-gray-800 rounded-lg hover:bg-gray-700 transition-colors">Cancel</button>
          <button onClick={handleEdit} className="px-4 py-2 text-sm text-[#0A0F1A] bg-[#00E5A0] rounded-lg hover:bg-[#00cc8e] transition-colors font-semibold">Save Changes</button>
        </div>
      </Modal>

      {/* Delete Confirm */}
      <ConfirmDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        onConfirm={handleDelete}
        message="Are you sure you want to delete this user? This action cannot be undone."
      />
    </div>
  );
}
