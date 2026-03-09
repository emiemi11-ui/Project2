import { useState, useMemo } from "react";
import {
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  BarChart, Bar, Cell, Legend,
} from "recharts";
import { persons, moodEntries } from "../../data/mockData";

/* ——————————————————————————————————————————————
   Theme tokens
   —————————————————————————————————————————————— */
const bg = "#1A1025";
const cardBg = "#241535";
const text = "#E8EEFF";
const accent = "#A78BFA";
const secondary = "#00E5A0";
const muted = "#8B8BA3";

/* ——————————————————————————————————————————————
   Helpers
   —————————————————————————————————————————————— */
const patients = persons.filter((p) =>
  ["soldier", "patient"].includes(p.role)
);

function moodToScore(mood) {
  const map = { great: 9, good: 7, okay: 5, low: 3, bad: 1 };
  return map[mood] ?? 5;
}

function scoreToColor(score) {
  if (score >= 7) return "#22C55E";
  if (score >= 5) return "#EAB308";
  if (score >= 3) return "#F97316";
  return "#EF4444";
}

function getDaysArray(n) {
  const arr = [];
  const today = new Date();
  for (let i = n - 1; i >= 0; i--) {
    const d = new Date(today);
    d.setDate(d.getDate() - i);
    arr.push(d.toISOString().split("T")[0]);
  }
  return arr;
}

/* ——————————————————————————————————————————————
   Mood Heatmap Cell
   —————————————————————————————————————————————— */
function HeatmapCell({ score, title }) {
  return (
    <div
      className="w-4 h-4 rounded-sm cursor-pointer transition-transform hover:scale-150"
      style={{ backgroundColor: score != null ? scoreToColor(score) : "#2D1F45" }}
      title={title}
    />
  );
}

/* ——————————————————————————————————————————————
   Component
   —————————————————————————————————————————————— */
export default function PsychMood() {
  const [range, setRange] = useState(7);
  const [groupFilter, setGroupFilter] = useState("all");

  const filteredPatients = useMemo(() => {
    if (groupFilter === "all") return patients;
    return patients.filter((p) => p.unit === groupFilter);
  }, [groupFilter]);

  const groups = useMemo(
    () => [...new Set(patients.map((p) => p.unit))],
    []
  );

  /* --- heatmap data (30 days x patients) --- */
  const heatmapDays = useMemo(() => getDaysArray(30), []);

  const heatmapData = useMemo(() => {
    return filteredPatients.map((p) => {
      const entries = moodEntries.filter((e) => e.personId === p.id);
      const dayScores = {};
      entries.forEach((e) => {
        dayScores[e.date] = moodToScore(e.mood);
      });
      return { name: p.name, id: p.id, dayScores };
    });
  }, [filteredPatients]);

  /* --- avg mood trend --- */
  const trendData = useMemo(() => {
    const days = getDaysArray(range);
    return days.map((date) => {
      const dayEntries = moodEntries
        .filter((e) => e.date === date && filteredPatients.some((p) => p.id === e.personId));
      const scores = dayEntries.map((e) => moodToScore(e.mood));
      const avg = scores.length ? +(scores.reduce((a, b) => a + b, 0) / scores.length).toFixed(1) : null;
      return { date: date.slice(5), avg };
    });
  }, [range, filteredPatients]);

  /* --- mood distribution (today) --- */
  const distribution = useMemo(() => {
    const today = new Date().toISOString().split("T")[0];
    const todayEntries = moodEntries.filter(
      (e) => e.date === today && filteredPatients.some((p) => p.id === e.personId)
    );
    const counts = Array.from({ length: 10 }, (_, i) => ({ score: i + 1, count: 0 }));
    todayEntries.forEach((e) => {
      const s = moodToScore(e.mood);
      const idx = Math.min(Math.max(s - 1, 0), 9);
      counts[idx].count += 1;
    });
    return counts;
  }, [filteredPatients]);

  /* --- patients needing attention --- */
  const needsAttention = useMemo(() => {
    return filteredPatients.filter((p) => {
      const latest = moodEntries
        .filter((e) => e.personId === p.id)
        .sort((a, b) => b.date.localeCompare(a.date))[0];
      const lowMood = latest && moodToScore(latest.mood) < 4;
      const highStress = p.metrics.stressScore > 80;
      return lowMood || highStress;
    });
  }, [filteredPatients]);

  return (
    <div className="min-h-screen p-6 space-y-8" style={{ backgroundColor: bg, color: text }}>
      {/* Header */}
      <div className="flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div>
          <h1 className="text-3xl font-bold" style={{ color: accent }}>
            Mood Overview
          </h1>
          <p style={{ color: muted }} className="mt-1">
            Monitor patient emotional well-being and mood patterns
          </p>
        </div>

        {/* Filters */}
        <div className="flex items-center gap-3 flex-wrap">
          <select
            value={range}
            onChange={(e) => setRange(Number(e.target.value))}
            className="rounded-lg px-3 py-2 text-sm font-medium outline-none"
            style={{ backgroundColor: cardBg, color: text, border: `1px solid ${accent}40` }}
          >
            <option value={7}>7 zile</option>
            <option value={30}>30 zile</option>
          </select>

          <select
            value={groupFilter}
            onChange={(e) => setGroupFilter(e.target.value)}
            className="rounded-lg px-3 py-2 text-sm font-medium outline-none"
            style={{ backgroundColor: cardBg, color: text, border: `1px solid ${accent}40` }}
          >
            <option value="all">Toate grupurile</option>
            {groups.map((g) => (
              <option key={g} value={g}>{g}</option>
            ))}
          </select>
        </div>
      </div>

      {/* Row 1: Heatmap */}
      <div className="rounded-2xl p-6" style={{ backgroundColor: cardBg }}>
        <h2 className="text-lg font-semibold mb-4" style={{ color: accent }}>
          Group Mood Heatmap
          <span className="ml-2 text-xs font-normal" style={{ color: muted }}>30 days x patients</span>
        </h2>
        <div className="overflow-x-auto">
          <div className="min-w-[700px]">
            {/* Day labels */}
            <div className="flex items-center mb-1 pl-36">
              {heatmapDays.filter((_, i) => i % 5 === 0).map((d) => (
                <span key={d} className="text-[10px] flex-1" style={{ color: muted }}>
                  {d.slice(5)}
                </span>
              ))}
            </div>
            {heatmapData.map((row) => (
              <div key={row.id} className="flex items-center gap-[2px] mb-[2px]">
                <span className="w-36 truncate text-xs pr-2" style={{ color: muted }}>
                  {row.name}
                </span>
                {heatmapDays.map((day) => (
                  <HeatmapCell
                    key={day}
                    score={row.dayScores[day] ?? null}
                    title={`${row.name} - ${day}: ${row.dayScores[day] ?? "N/A"}`}
                  />
                ))}
              </div>
            ))}
            {/* Legend */}
            <div className="flex items-center gap-3 mt-3 pl-36">
              <span className="text-[10px]" style={{ color: muted }}>Low</span>
              {["#EF4444", "#F97316", "#EAB308", "#22C55E"].map((c) => (
                <div key={c} className="w-3 h-3 rounded-sm" style={{ backgroundColor: c }} />
              ))}
              <span className="text-[10px]" style={{ color: muted }}>High</span>
            </div>
          </div>
        </div>
      </div>

      {/* Row 2: Trend + Distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Average mood trend */}
        <div className="rounded-2xl p-6" style={{ backgroundColor: cardBg }}>
          <h2 className="text-lg font-semibold mb-4" style={{ color: accent }}>
            Average Mood Trend
          </h2>
          <ResponsiveContainer width="100%" height={260}>
            <LineChart data={trendData}>
              <CartesianGrid strokeDasharray="3 3" stroke="#3B2D55" />
              <XAxis dataKey="date" tick={{ fill: muted, fontSize: 11 }} />
              <YAxis domain={[1, 10]} tick={{ fill: muted, fontSize: 11 }} />
              <Tooltip
                contentStyle={{ backgroundColor: cardBg, border: `1px solid ${accent}`, color: text, borderRadius: 12 }}
              />
              <Line
                type="monotone"
                dataKey="avg"
                stroke={accent}
                strokeWidth={3}
                dot={{ fill: accent, r: 4 }}
                connectNulls
                name="Avg Mood"
              />
            </LineChart>
          </ResponsiveContainer>
        </div>

        {/* Mood distribution today */}
        <div className="rounded-2xl p-6" style={{ backgroundColor: cardBg }}>
          <h2 className="text-lg font-semibold mb-4" style={{ color: accent }}>
            Mood Distribution Today
          </h2>
          <ResponsiveContainer width="100%" height={260}>
            <BarChart data={distribution}>
              <CartesianGrid strokeDasharray="3 3" stroke="#3B2D55" />
              <XAxis dataKey="score" tick={{ fill: muted, fontSize: 11 }} />
              <YAxis allowDecimals={false} tick={{ fill: muted, fontSize: 11 }} />
              <Tooltip
                contentStyle={{ backgroundColor: cardBg, border: `1px solid ${accent}`, color: text, borderRadius: 12 }}
              />
              <Bar dataKey="count" radius={[6, 6, 0, 0]} name="Patients">
                {distribution.map((entry, idx) => (
                  <Cell key={idx} fill={scoreToColor(entry.score)} />
                ))}
              </Bar>
            </BarChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Row 3: Patients needing attention */}
      <div className="rounded-2xl p-6" style={{ backgroundColor: cardBg }}>
        <h2 className="text-lg font-semibold mb-4" style={{ color: accent }}>
          Patients Needing Attention
          <span className="ml-2 text-xs font-normal" style={{ color: muted }}>
            mood &lt; 4 or stress &gt; 80
          </span>
        </h2>

        {needsAttention.length === 0 ? (
          <p className="text-sm" style={{ color: muted }}>
            No patients currently flagged.
          </p>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
            {needsAttention.map((p) => {
              const latest = moodEntries
                .filter((e) => e.personId === p.id)
                .sort((a, b) => b.date.localeCompare(a.date))[0];
              return (
                <div
                  key={p.id}
                  className="rounded-xl p-4 border transition-colors"
                  style={{ backgroundColor: bg, borderColor: "#EF444440" }}
                >
                  <div className="flex items-center gap-3 mb-2">
                    <img
                      src={p.avatar}
                      alt={p.name}
                      className="w-10 h-10 rounded-full"
                      style={{ backgroundColor: "#3B2D55" }}
                    />
                    <div>
                      <p className="font-medium text-sm">{p.name}</p>
                      <p className="text-xs" style={{ color: muted }}>{p.unit}</p>
                    </div>
                  </div>
                  <div className="flex items-center gap-4 text-xs mt-2">
                    <span>
                      Mood:{" "}
                      <span style={{ color: latest ? scoreToColor(moodToScore(latest.mood)) : muted }}>
                        {latest ? latest.mood : "N/A"}
                      </span>
                    </span>
                    <span>
                      Stress:{" "}
                      <span style={{ color: p.metrics.stressScore > 80 ? "#EF4444" : secondary }}>
                        {p.metrics.stressScore}%
                      </span>
                    </span>
                  </div>
                  {latest?.note && (
                    <p className="text-xs mt-2 italic" style={{ color: muted }}>
                      &ldquo;{latest.note}&rdquo;
                    </p>
                  )}
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
}
