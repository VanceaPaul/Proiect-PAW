import { Navigate, Route, Routes } from 'react-router-dom';
import LoginPage from './pages/LoginPage.jsx';
import RegisterPage from './pages/RegisterPage.jsx';
import ProfessorDashboard from './pages/ProfessorDashboard.jsx';
import StudentDashboard from './pages/StudentDashboard.jsx';
import { useAuth } from './AuthContext.jsx';

const RequireAuth = ({ role, children }) => {
  const { token, role: currentRole } = useAuth();
  if (!token) {
    return <Navigate to="/login" replace />;
  }
  if (role && role !== currentRole) {
    return <Navigate to={currentRole === 'PROFESSOR' ? '/professor' : '/student'} replace />;
  }
  return children;
};

const App = () => {
  const { token, role } = useAuth();

  return (
    <Routes>
      <Route
        path="/"
        element={token ? <Navigate to={role === 'PROFESSOR' ? '/professor' : '/student'} replace /> : <Navigate to="/login" replace />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />
      <Route
        path="/professor"
        element={(
          <RequireAuth role="PROFESSOR">
            <ProfessorDashboard />
          </RequireAuth>
        )}
      />
      <Route
        path="/student"
        element={(
          <RequireAuth role="STUDENT">
            <StudentDashboard />
          </RequireAuth>
        )}
      />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
};

export default App;
