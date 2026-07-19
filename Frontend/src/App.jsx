import { BrowserRouter as Router, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { ToastProvider } from '@/components/Elements/Toast';
import AppRoutes from '@/routes/AppRoutes';
import useAuthStore from '@/store/useAuthStore';
import { authApi } from '@/features/auth/api';

// Scroll to top on route change (instant, not smooth)
function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' });
  }, [pathname]);
  return null;
}

function App() {
  const { token, user, setUser, logout } = useAuthStore();

  useEffect(() => {
    if (token && !user) {
      authApi.getMyInfo()
        .then((res) => {
          setUser(res.result || res);
        })
        .catch((err) => {
          console.error('Failed to restore session:', err);
          logout();
        });
    }
  }, [token, user, setUser, logout]);

  return (
    <ToastProvider>
      <Router>
        <ScrollToTop />
        <AppRoutes />
      </Router>
    </ToastProvider>
  );
}

export default App;
