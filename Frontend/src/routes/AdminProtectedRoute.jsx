import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import useAuthStore from '@/store/useAuthStore';

const AdminProtectedRoute = ({ children }) => {
    const { token, user } = useAuthStore();

    // Show loading spinner while fetching user info on page reload
    if (token && !user) {
        return (
            <div className="min-h-screen flex items-center justify-center bg-gray-50">
                <div className="w-10 h-10 border-4 border-primary/20 border-t-primary rounded-full animate-spin"></div>
            </div>
        );
    }

    if (!token) {
        return <Navigate to="/admin/login" replace />;
    }

    if (user?.account?.role?.name !== 'ADMIN' && user?.role?.name !== 'ADMIN') {
        return <Navigate to="/" replace />;
    }

    return children ? children : <Outlet />;
};

export default AdminProtectedRoute;
