import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuthStore from '@/shared/store/useAuthStore';

const AdminProtectedRoute = ({ children }) => {
    const { token, user } = useAuthStore();

    if (!token) {
        return <Navigate to="/admin/login" replace />;
    }

    const role = user?.account?.role?.name
        || user?.account?.role
        || user?.role?.name
        || user?.role;
    if (role !== 'ADMIN') {
        return <Navigate to="/" replace />;
    }

    return children ? children : <Outlet />;
};

export default AdminProtectedRoute;
