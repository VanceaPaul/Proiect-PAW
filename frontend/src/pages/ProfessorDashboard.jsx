import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api.js';
import { useAuth } from '../AuthContext.jsx';
import LiveStatsPanel from '../components/LiveStatsPanel.jsx';

const emptyOption = () => ({ text: '', correct: false });

const ProfessorDashboard = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [questions, setQuestions] = useState([]);
  const [editingQuestionId, setEditingQuestionId] = useState(null);
  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [questionForm, setQuestionForm] = useState({ text: '', options: [emptyOption(), emptyOption()] });
  const [sessionForm, setSessionForm] = useState({ title: '', durationSeconds: 300, questionIds: new Set() });
  const [activeSessionId, setActiveSessionId] = useState(null);
  const [activeQuestionId, setActiveQuestionId] = useState(null);
  const [editingSessionId, setEditingSessionId] = useState(null);
  const [expandedSessionId, setExpandedSessionId] = useState(null);
  const [scoreboards, setScoreboards] = useState({});
  const [scoreLoading, setScoreLoading] = useState({});
  const [sessionMessage, setSessionMessage] = useState(null);
  const [sessionSubmitting, setSessionSubmitting] = useState(false);

  const selectedQuestionIds = useMemo(() => Array.from(sessionForm.questionIds), [sessionForm.questionIds]);

  const loadData = async () => {
    try {
      const [questionsResponse, sessionsResponse] = await Promise.all([
        api.get('/questions'),
        api.get('/professor/sessions')
      ]);
      setQuestions(questionsResponse.data);
      setSessions(sessionsResponse.data);
    } catch (error) {
      console.error(error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, []);

  const addOption = () => {
    setQuestionForm((prev) => ({ ...prev, options: [...prev.options, emptyOption()] }));
  };

  const removeOption = (index) => {
    setQuestionForm((prev) => ({
      ...prev,
      options: prev.options.filter((_, idx) => idx !== index)
    }));
  };

  const updateOption = (index, changes) => {
    setQuestionForm((prev) => ({
      ...prev,
      options: prev.options.map((option, idx) => (idx === index ? { ...option, ...changes } : option))
    }));
  };

  const submitQuestion = async (event) => {
    event.preventDefault();
    if (editingQuestionId) {
      await api.put(`/questions/${editingQuestionId}`, questionForm);
    } else {
      await api.post('/questions', questionForm);
    }
    setQuestionForm({ text: '', options: [emptyOption(), emptyOption()] });
    setEditingQuestionId(null);
    loadData();
  };

  const deleteQuestion = async (id) => {
    await api.delete(`/questions/${id}`);
    loadData();
  };

  const startEditQuestion = (question) => {
    setEditingQuestionId(question.id);
    setQuestionForm({
      text: question.text,
      options: question.options.map((option) => ({
        id: option.id,
        text: option.text,
        correct: option.correct
      }))
    });
  };

  const cancelEdit = () => {
    setEditingQuestionId(null);
    setQuestionForm({ text: '', options: [emptyOption(), emptyOption()] });
  };

  const resetSessionForm = () => {
    setSessionForm({ title: '', durationSeconds: 300, questionIds: new Set() });
    setEditingSessionId(null);
  };

  const toggleQuestionSelection = (id) => {
    setSessionForm((prev) => {
      const next = new Set(prev.questionIds);
      if (next.has(id)) {
        next.delete(id);
      } else {
        next.add(id);
      }
      return { ...prev, questionIds: next };
    });
  };

  const submitSession = async (event) => {
    event.preventDefault();
    const trimmedTitle = sessionForm.title.trim();
    if (!trimmedTitle) {
      setSessionMessage({ type: 'error', text: 'Session title is required.' });
      return;
    }
    if (selectedQuestionIds.length === 0) {
      setSessionMessage({ type: 'error', text: 'Select at least one question.' });
      return;
    }
    setSessionMessage(null);
    setSessionSubmitting(true);
    const payload = {
      title: trimmedTitle,
      durationSeconds: Number(sessionForm.durationSeconds),
      questionIds: selectedQuestionIds
    };
    const currentEditingId = editingSessionId;
    try {
      if (currentEditingId) {
        await api.put(`/professor/sessions/${currentEditingId}`, payload);
        setSessionMessage({ type: 'success', text: 'Session updated.' });
      } else {
        await api.post('/professor/sessions', payload);
        setSessionMessage({ type: 'success', text: 'Session created.' });
      }
      if (currentEditingId) {
        setScoreboards((prev) => {
          const next = { ...prev };
          delete next[currentEditingId];
          return next;
        });
        if (expandedSessionId === currentEditingId) {
          setExpandedSessionId(null);
        }
      }
      resetSessionForm();
      await loadData();
    } catch (error) {
      console.error(error);
      setSessionMessage({ type: 'error', text: error.response?.data?.message || 'Failed to save session.' });
    } finally {
      setSessionSubmitting(false);
    }
  };

  const startEditSession = (session) => {
    setSessionForm({
      title: session.title,
      durationSeconds: session.durationSeconds,
      questionIds: new Set((session.questions || []).map((question) => question.id))
    });
    setEditingSessionId(session.id);
    setSessionMessage(null);
  };

  const cancelSessionEdit = () => {
    resetSessionForm();
    setSessionMessage(null);
  };

  const deleteSession = async (sessionId) => {
    try {
      await api.delete(`/professor/sessions/${sessionId}`);
      setSessionMessage({ type: 'success', text: 'Session deleted.' });
      if (editingSessionId === sessionId) {
        resetSessionForm();
      }
      if (expandedSessionId === sessionId) {
        setExpandedSessionId(null);
      }
      if (activeSessionId === sessionId) {
        setActiveSessionId(null);
        setActiveQuestionId(null);
      }
      setScoreboards((prev) => {
        const next = { ...prev };
        delete next[sessionId];
        return next;
      });
      setScoreLoading((prev) => {
        const next = { ...prev };
        delete next[sessionId];
        return next;
      });
      await loadData();
    } catch (error) {
      console.error(error);
      setSessionMessage({ type: 'error', text: error.response?.data?.message || 'Failed to delete session.' });
    }
  };

  const toggleScoreboard = async (sessionId) => {
    if (expandedSessionId === sessionId) {
      setExpandedSessionId(null);
      return;
    }
    setExpandedSessionId(sessionId);
    if (scoreboards[sessionId]) {
      return;
    }
    setScoreLoading((prev) => ({ ...prev, [sessionId]: true }));
    try {
      const { data } = await api.get(`/professor/sessions/${sessionId}/scores`);
      setScoreboards((prev) => ({ ...prev, [sessionId]: data }));
    } catch (error) {
      console.error(error);
      setSessionMessage({ type: 'error', text: error.response?.data?.message || 'Failed to load scores.' });
    } finally {
      setScoreLoading((prev) => ({ ...prev, [sessionId]: false }));
    }
  };

  const activateSession = async (sessionId) => {
    await api.post(`/professor/sessions/${sessionId}/activate`);
    loadData();
  };

  const closeSession = async (sessionId) => {
    await api.post(`/professor/sessions/${sessionId}/close`);
    loadData();
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (loading) {
    return <main>Loading...</main>;
  }

  return (
    <main>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Professor Dashboard</h1>
        <button onClick={handleLogout}>Logout</button>
      </header>

      <section className="card">
        <h2>Create Question</h2>
        <form onSubmit={submitQuestion}>
          <label>
            Question text
            <input value={questionForm.text} onChange={(e) => setQuestionForm((prev) => ({ ...prev, text: e.target.value }))} required />
          </label>
          <h3>Answer options</h3>
          {questionForm.options.map((option, index) => (
            <div key={index} style={{ display: 'flex', gap: '0.5rem', alignItems: 'center', marginBottom: '0.5rem' }}>
              <input
                placeholder={`Option ${index + 1}`}
                value={option.text}
                onChange={(e) => updateOption(index, { text: e.target.value })}
                required
              />
              <label style={{ display: 'flex', alignItems: 'center', gap: '0.25rem' }}>
                <input
                  type="checkbox"
                  checked={option.correct}
                  onChange={(e) => updateOption(index, { correct: e.target.checked })}
                />
                Correct
              </label>
              {questionForm.options.length > 2 && (
                <button type="button" onClick={() => removeOption(index)}>
                  Remove
                </button>
              )}
            </div>
          ))}
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button type="button" onClick={addOption}>
              Add option
            </button>
            <button type="submit">{editingQuestionId ? 'Update question' : 'Save question'}</button>
            {editingQuestionId && (
              <button type="button" onClick={cancelEdit}>
                Cancel
              </button>
            )}
          </div>
        </form>
      </section>

      <section className="card">
        <h2>Questions</h2>
        {questions.length === 0 && <p>No questions yet.</p>}
        {questions.map((question) => (
          <div key={question.id} style={{ borderBottom: '1px solid #e4e4e7', padding: '0.5rem 0' }}>
            <strong>{question.text}</strong>
            <ul>
              {question.options.map((option) => (
                <li key={option.id ?? option.text}>
                  {option.text} {option.correct ? '(correct)' : ''}
                </li>
              ))}
            </ul>
            <div style={{ display: 'flex', gap: '0.5rem' }}>
              <label style={{ display: 'flex', gap: '0.25rem', alignItems: 'center' }}>
                <input
                  type="checkbox"
                  checked={sessionForm.questionIds.has(question.id)}
                  onChange={() => toggleQuestionSelection(question.id)}
                />
                Include in session
              </label>
              <button type="button" onClick={() => deleteQuestion(question.id)}>
                Delete
              </button>
              <button type="button" onClick={() => startEditQuestion(question)}>
                Edit
              </button>
            </div>
          </div>
        ))}
      </section>

      <section className="card">
        <h2>{editingSessionId ? 'Edit Session' : 'Create Session'}</h2>
        <form onSubmit={submitSession}>
          <label>
            Title
            <input value={sessionForm.title} onChange={(e) => setSessionForm((prev) => ({ ...prev, title: e.target.value }))} required />
          </label>
          <label>
            Duration seconds
            <input
              type="number"
              min={60}
              value={sessionForm.durationSeconds}
              onChange={(e) => setSessionForm((prev) => ({ ...prev, durationSeconds: e.target.value }))}
            />
          </label>
          <p>Selected questions: {selectedQuestionIds.length}</p>
          <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
            <button type="submit" disabled={sessionSubmitting || selectedQuestionIds.length === 0}>
              {sessionSubmitting ? 'Saving…' : editingSessionId ? 'Update session' : 'Create session'}
            </button>
            {editingSessionId && (
              <button type="button" onClick={cancelSessionEdit} disabled={sessionSubmitting}>
                Cancel
              </button>
            )}
          </div>
        </form>
        {sessionMessage && (
          <p style={{ color: sessionMessage.type === 'success' ? '#16a34a' : 'crimson', marginTop: '0.75rem' }}>
            {sessionMessage.text}
          </p>
        )}
      </section>

      <section className="card">
        <h2>Sessions</h2>
        {sessions.length === 0 && <p>No sessions yet.</p>}
        {sessions.map((session) => {
          const isExpanded = expandedSessionId === session.id;
          const scores = scoreboards[session.id] || [];
          const isScoresLoading = scoreLoading[session.id];
          return (
            <div key={session.id} style={{ borderBottom: '1px solid #e4e4e7', padding: '0.75rem 0' }}>
              <div style={{ display: 'flex', justifyContent: 'space-between', flexWrap: 'wrap', gap: '0.75rem' }}>
                <div>
                  <strong>{session.title}</strong>
                  <p>Access code: {session.accessCode}</p>
                  <p>Status: {session.status}</p>
                  <p>Total questions: {session.questions?.length ?? 0}</p>
                </div>
                <div style={{ display: 'flex', gap: '0.5rem', flexWrap: 'wrap' }}>
                  <button
                    type="button"
                    onClick={() => activateSession(session.id)}
                    disabled={session.status === 'ACTIVE'}
                  >
                    Activate
                  </button>
                  <button
                    type="button"
                    onClick={() => closeSession(session.id)}
                    disabled={session.status === 'CLOSED'}
                  >
                    Close
                  </button>
                  <button type="button" onClick={() => startEditSession(session)}>
                    Edit
                  </button>
                  <button type="button" onClick={() => deleteSession(session.id)}>
                    Delete
                  </button>
                  <button type="button" onClick={() => toggleScoreboard(session.id)}>
                    {isExpanded ? 'Hide scores' : 'View scores'}
                  </button>
                </div>
              </div>
              <div style={{ marginTop: '0.5rem' }}>
                <label>
                  Live stats question
                  <select
                    value={session.id === activeSessionId ? activeQuestionId ?? '' : ''}
                    onChange={(event) => {
                      const selectedId = Number(event.target.value);
                      setActiveSessionId(session.id);
                      setActiveQuestionId(selectedId || null);
                    }}
                  >
                    <option value="">Select question</option>
                    {session.questions?.map((question) => (
                      <option key={question.id} value={question.id}>
                        {question.text.slice(0, 60)}
                      </option>
                    ))}
                  </select>
                </label>
              </div>
              {isExpanded && (
                <div className="scoreboard">
                  {isScoresLoading ? (
                    <p>Loading scores...</p>
                  ) : scores.length ? (
                    <table className="scoreboard-table">
                      <thead>
                        <tr>
                          <th>Student</th>
                          <th>Score</th>
                          <th>Status</th>
                        </tr>
                      </thead>
                      <tbody>
                        {scores.map((entry) => (
                          <tr key={entry.studentId ?? entry.studentEmail}>
                            <td>
                              <span className="scoreboard-name">{entry.studentName}</span>
                              <span className="scoreboard-email">{entry.studentEmail}</span>
                            </td>
                            <td>
                              {entry.correct} / {entry.total}
                            </td>
                            <td>{entry.submitted ? 'Submitted' : 'In progress'}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  ) : (
                    <p className="scoreboard-empty">No submissions yet.</p>
                  )}
                </div>
              )}
            </div>
          );
        })}
      </section>

      <LiveStatsPanel sessionId={activeSessionId} questionId={activeQuestionId} />
    </main>
  );
};

export default ProfessorDashboard;
