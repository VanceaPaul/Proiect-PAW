import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api.js';
import { useAuth } from '../AuthContext.jsx';

const RegisterPage = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [role, setRole] = useState('STUDENT');
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    try {
      const trimmedName = fullName.trim();
      if (!trimmedName) {
        setError('Full name is required');
        return;
      }
      const { data } = await api.post('/auth/register', { email, password, role, fullName: trimmedName });
      login(data.token);
      if (role === 'PROFESSOR') {
        navigate('/professor');
      } else {
        navigate('/student');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Registration failed');
    }
  };

  return (
    <main>
      <div className="card" style={{ maxWidth: 420, margin: '0 auto' }}>
        <h1>Register</h1>
        <form onSubmit={handleSubmit}>
          <label>
            Full name
            <input value={fullName} onChange={(e) => setFullName(e.target.value)} required />
          </label>
          <label>
            Email
            <input value={email} onChange={(e) => setEmail(e.target.value)} type="email" required />
          </label>
          <label>
            Password
            <input value={password} onChange={(e) => setPassword(e.target.value)} type="password" required />
          </label>
          <label>
            Role
            <select value={role} onChange={(e) => setRole(e.target.value)}>
              <option value="STUDENT">Student</option>
              <option value="PROFESSOR">Professor</option>
            </select>
          </label>
          {error && <p style={{ color: 'crimson' }}>{error}</p>}
          <button type="submit">Register</button>
        </form>
        <p>
          Already have an account? <Link to="/login">Login</Link>
        </p>
      </div>
    </main>
  );
};

export default RegisterPage;
