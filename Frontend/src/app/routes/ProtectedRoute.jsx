// ============================================================
// src/routes/ProtectedRoute.jsx
// Guard component: chặn truy cập nếu chưa đăng nhập
// Moved from: src/components/ProtectedRoute.jsx
// ============================================================
import { Navigate, useLocation } from 'react-router-dom';

/**
 * @param {Object} props
 * @param {React.ReactNode} props.children
 * @param {string[]} [props.allowedRoles] - Nếu truyền vào, kiểm tra thêm role từ localStorage
 */
const ProtectedRoute = ({ children, allowedRoles }) => {
  const token = localStorage.getItem('token');
  const location = useLocation();

  if (!token) {
    // Lưu lại URL đang cố vào để sau khi login redirect về
    return <Navigate to="/login" state={{ from: location }} replace />;
  }

  return children;
};

export default ProtectedRoute;
