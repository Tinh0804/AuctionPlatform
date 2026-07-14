import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuthStore from '@/store/useAuthStore';

const AdminProtectedRoute = ({ children }) => {
    const { token, user } = useAuthStore();

    if (!token) {
        return <Navigate to="/admin/login" replace />;
    }

    if (user?.account?.role?.name !== 'ADMIN' && user?.role?.name !== 'ADMIN') {
        return <Navigate to="/" replace />;
    }

    return children ? children : <Outlet />;
};

export default AdminProtectedRoute;
