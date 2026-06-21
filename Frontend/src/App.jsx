import { BrowserRouter as Router, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { ToastProvider } from '@/components/Elements/Toast';
import AppRoutes from '@/routes/AppRoutes';

// Scroll to top on route change (instant, not smooth)
function ScrollToTop() {
  const { pathname } = useLocation();
  useEffect(() => {
    window.scrollTo({ top: 0, behavior: 'instant' });
  }, [pathname]);
  return null;
}

function App() {
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
