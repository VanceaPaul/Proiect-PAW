import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import api from '../services/api.js';
import { useAuth } from '../AuthContext.jsx';

const LoginPage = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(null);
  const navigate = useNavigate();
  const { login } = useAuth();

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError(null);
    try {
      const { data } = await api.post('/auth/login', { email, password });
      login(data.token);
      const payload = JSON.parse(atob(data.token.split('.')[1]));
      if (payload.role === 'PROFESSOR') {
        navigate('/professor');
      } else {
        navigate('/student');
      }
    } catch (err) {
      setError(err.response?.data?.message || 'Login failed');
    }
  };

  return (
    <main>
      <div className="card" style={{ maxWidth: 420, margin: '0 auto' }}>
        <h1>Login</h1>
        <form onSubmit={handleSubmit}>
          <label>
            Email
            <input value={email} onChange={(event) => setEmail(event.target.value)} type="email" required />
          </label>
          <label>
            Password
            <input value={password} onChange={(event) => setPassword(event.target.value)} type="password" required />
          </label>
          {error && <p style={{ color: 'crimson' }}>{error}</p>}
          <button type="submit">Login</button>
        </form>
        <p>
          No account? <Link to="/register">Register</Link>
        </p>
      </div>
    </main>
  );
};

export default LoginPage;
