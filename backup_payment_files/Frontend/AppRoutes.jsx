// ============================================================
// src/routes/AppRoutes.jsx
// Tập trung toàn bộ định nghĩa route của ứng dụng
// ============================================================
import { Routes, Route } from 'react-router-dom';
import MainLayout from '@/layouts/MainLayout';
import AuthLayout from '@/layouts/AuthLayout';
import ProtectedRoute from '@/routes/ProtectedRoute';

// Public pages
import Home from '@/pages/public/Home';
import AuctionList from '@/pages/public/AuctionList';
import AuctionDetail from '@/pages/public/AuctionDetail';

// Auth pages
import Login from '@/pages/auth/Login';
import Register from '@/pages/auth/Register';

import ProfileLayout from '@/pages/bidder/profile/ProfileLayout';
import PersonalPage from '@/pages/bidder/profile/PersonalPage';
import WalletPage from '@/pages/bidder/profile/WalletPage';
import OrdersPage from '@/pages/bidder/profile/OrdersPage';
import SupportPage from '@/pages/bidder/profile/SupportPage';
import SettingsPage from '@/pages/bidder/profile/SettingsPage';

import EKyc from '@/pages/bidder/EKyc';
import Checkout from '@/pages/bidder/Checkout';
import PaymentResult from '@/pages/bidder/PaymentResult';
import OrderPayPage from '@/pages/bidder/OrderPayPage';

// Seller pages
import CreateAuction from '@/pages/seller/CreateAuction';

// Fallback
import NotFound from '@/pages/NotFound';

const AppRoutes = () => {
  return (
    <Routes>
      {/* ── Auth Layout ── */}
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<Login />} />
        <Route path="/register" element={<Register />} />
      </Route>

      {/* ── Main Layout ── */}
      <Route element={<MainLayout />}>
        {/* Public routes */}
        <Route index element={<Home />} />
        <Route path="auctions" element={<AuctionList />} />
        <Route path="auctions/:id" element={<AuctionDetail />} />

        {/* Protected: Bidder */}
        <Route path="profile" element={<ProtectedRoute><ProfileLayout /></ProtectedRoute>}>
          <Route index element={<PersonalPage />} />
          <Route path="personal" element={<PersonalPage />} />
          <Route path="wallet" element={<WalletPage />} />
          <Route path="orders" element={<OrdersPage />} />
          <Route path="support" element={<SupportPage />} />
          <Route path="settings" element={<SettingsPage />} />
        </Route>
        <Route path="ekyc" element={<ProtectedRoute><EKyc /></ProtectedRoute>} />
        <Route path="invoices/:invoice_id/checkout" element={<ProtectedRoute><Checkout /></ProtectedRoute>} />
        <Route path="orders/:orderId/pay" element={<ProtectedRoute><OrderPayPage /></ProtectedRoute>} />
        
        <Route path="wallets/deposit/momo-return" element={<ProtectedRoute><PaymentResult /></ProtectedRoute>} />
        <Route path="payment/:status" element={<ProtectedRoute><PaymentResult /></ProtectedRoute>} />

        {/* Protected: Seller */}
        <Route path="auctions/create" element={<ProtectedRoute><CreateAuction /></ProtectedRoute>} />

        {/* 404 */}
        <Route path="*" element={<NotFound />} />
      </Route>
    </Routes>
  );
};

export default AppRoutes;
