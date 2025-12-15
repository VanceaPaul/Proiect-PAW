import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import api from '../services/api.js';
import { useAuth } from '../AuthContext.jsx';

const StudentDashboard = () => {
  const { logout } = useAuth();
  const navigate = useNavigate();
  const [accessCode, setAccessCode] = useState('');
  const [session, setSession] = useState(null);
  const [answers, setAnswers] = useState({});
  const [score, setScore] = useState(null);
  const [showResults, setShowResults] = useState(false);
  const [message, setMessage] = useState(null);
  const [fullName, setFullName] = useState('');
  const [profileLoading, setProfileLoading] = useState(true);
  const [savingName, setSavingName] = useState(false);
  const [profileMessage, setProfileMessage] = useState(null);

  useEffect(() => {
    let active = true;
    const loadProfile = async () => {
      try {
        const { data } = await api.get('/auth/profile');
        if (active) {
          setFullName(data.fullName || '');
        }
      } catch (error) {
        if (active) {
          setProfileMessage({ type: 'error', text: 'Failed to load name.' });
        }
      } finally {
        if (active) {
          setProfileLoading(false);
        }
      }
    };
    loadProfile();

    return () => {
      active = false;
    };
  }, []);

  const handleNameSubmit = async (event) => {
    event.preventDefault();
    const trimmed = fullName.trim();
    if (!trimmed) {
      setProfileMessage({ type: 'error', text: 'Please enter your name.' });
      return;
    }
    setProfileMessage(null);
    setSavingName(true);
    try {
      await api.put('/auth/profile', { fullName: trimmed });
      setFullName(trimmed);
      setProfileMessage({ type: 'success', text: 'Name saved.' });
    } catch (error) {
      setProfileMessage({ type: 'error', text: error.response?.data?.message || 'Failed to save name.' });
    } finally {
      setSavingName(false);
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const loadSession = async (event) => {
    event.preventDefault();
    try {
      const { data } = await api.get(`/student/sessions/${accessCode}`);
      setSession(data);
      setAnswers({});
      setScore(null);
      setShowResults(false);
      setMessage(null);
    } catch (error) {
      setMessage(error.response?.data?.message || 'Failed to find session.');
    }
  };

  const selectAnswer = (questionId, optionId) => {
    if (showResults) {
      return;
    }
    setAnswers((prev) => ({ ...prev, [questionId]: Number(optionId) }));
  };

  const submitAnswers = async () => {
    const payload = Object.entries(answers).map(([questionId, optionId]) => ({
      questionId: Number(questionId),
      optionId: Number(optionId)
    }));
    if (payload.length === 0) {
      setMessage('Please answer at least one question.');
      return;
    }
    try {
      await api.post(`/student/sessions/${accessCode}/answers`, { answers: payload });
      setMessage('Answers submitted!');
      setShowResults(false);
      setScore(null);
    } catch (error) {
      setMessage(error.response?.data?.message || 'Failed to submit answers.');
    }
  };

  const fetchScore = async () => {
    try {
      const { data } = await api.get(`/student/sessions/${accessCode}/score`);
      setScore(data);
      setShowResults(true);
    } catch (error) {
      setMessage(error.response?.data?.message || 'Failed to retrieve score.');
    }
  };

  return (
    <main>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
        <h1>Student Dashboard</h1>
        <button onClick={handleLogout}>Logout</button>
      </header>

      <section className="card" style={{ maxWidth: 420 }}>
        <h2>Your name</h2>
        {profileLoading ? (
          <p>Loading profile...</p>
        ) : (
          <form onSubmit={handleNameSubmit}>
            <label>
              Display name
              <input
                value={fullName}
                onChange={(event) => setFullName(event.target.value)}
                disabled={savingName}
                required
              />
            </label>
            <button type="submit" disabled={savingName}>
              {savingName ? 'Saving…' : 'Save name'}
            </button>
          </form>
        )}
        {profileMessage && (
          <p style={{ color: profileMessage.type === 'success' ? '#16a34a' : 'crimson' }}>{profileMessage.text}</p>
        )}
      </section>

      <section className="card" style={{ maxWidth: 420 }}>
        <form onSubmit={loadSession}>
          <label>
            Join with access code
            <input value={accessCode} onChange={(event) => setAccessCode(event.target.value.toUpperCase())} required />
          </label>
          <button type="submit">Load session</button>
        </form>
        {message && <p>{message}</p>}
      </section>

      {session && (
        <section className="card">
          <h2>{session.title}</h2>
          {session.questions?.map((question) => {
            const questionResult = score?.results?.find((item) => item.questionId === question.id);
            const selectedOptionId = showResults
              ? questionResult?.selectedOptionId
              : answers[question.id];
            const correctIds = questionResult?.correctOptionIds ?? [];

            return (
              <div key={question.id} style={{ marginBottom: '1.25rem' }}>
                <strong>{question.text}</strong>
                <div className="option-grid">
                  {question.options.map((option) => {
                    const isSelected = selectedOptionId === option.id;
                    const isCorrectOption = correctIds.includes(option.id);
                    const isWrongSelection = showResults && questionResult?.answered && isSelected && !questionResult.correct;

                    let className = 'option-box';
                    if (showResults) {
                      if (isCorrectOption) {
                        className += ' correct';
                      }
                      if (isWrongSelection) {
                        className += ' incorrect';
                      }
                      if (!questionResult?.answered && !isCorrectOption) {
                        className += ' disabled';
                      }
                      if (questionResult?.answered && isSelected && !isWrongSelection && !isCorrectOption) {
                        className += ' disabled';
                      }
                    } else if (isSelected) {
                      className += ' selected';
                    }

                    const handleClick = () => selectAnswer(question.id, option.id);

                    return (
                      <button
                        key={option.id}
                        type="button"
                        className={className}
                        onClick={handleClick}
                        disabled={showResults}
                      >
                        {option.text}
                      </button>
                    );
                  })}
                </div>
              </div>
            );
          })}
          <div style={{ display: 'flex', gap: '0.5rem' }}>
            <button onClick={submitAnswers}>Submit answers</button>
            <button type="button" onClick={fetchScore}>
              View score
            </button>
          </div>
          {score && (
            <div style={{ marginTop: '1rem' }}>
              <p>
                Score: {score.correct} / {score.total}
              </p>
              {score.results && (
                <ul style={{ marginTop: '0.5rem' }}>
                  {score.results.map((item) => {
                    const status = item.answered ? (item.correct ? 'Correct' : 'Incorrect') : 'Not answered';
                    const color = item.answered ? (item.correct ? 'green' : 'crimson') : '#555';
                    return (
                      <li key={item.questionId} style={{ color }}>
                        {item.question} — {status}
                      </li>
                    );
                  })}
                </ul>
              )}
            </div>
          )}
        </section>
      )}
    </main>
  );
};

export default StudentDashboard;
