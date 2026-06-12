import { BrowserRouter as Router, Routes, Route, useLocation } from 'react-router-dom';
import { useEffect } from 'react';
import { ToastProvider } from './components/Toast';
import Layout from './components/Layout';
import Home from './pages/Home';
import AuctionList from './pages/AuctionList';
import AuctionDetail from './pages/AuctionDetail';
import CreateAuction from './pages/CreateAuction';
import Checkout from './pages/Checkout';
import Login from './pages/Login';
import Profile from './pages/Profile';
import EKyc from './pages/EKyc';
import PaymentResult from './pages/PaymentResult';

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
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<Home />} />
            <Route path="login" element={<Login />} />
            <Route path="profile" element={<Profile />} />
            <Route path="ekyc" element={<EKyc />} />
            <Route path="auctions" element={<AuctionList />} />
            <Route path="auctions/create" element={<CreateAuction />} />
            <Route path="auctions/:id" element={<AuctionDetail />} />
            <Route path="invoices/:invoice_id/checkout" element={<Checkout />} />
            <Route path="wallets/deposit/momo-return" element={<PaymentResult />} />
            <Route path="payment/:status" element={<PaymentResult />} />
          </Route>
        </Routes>
      </Router>
    </ToastProvider>
  )
}

export default App;
