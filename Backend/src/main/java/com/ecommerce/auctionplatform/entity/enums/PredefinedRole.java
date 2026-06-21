package com.ecommerce.auctionplatform.entity.enums;

public enum PredefinedRole {
    ADMIN(RoleName.ADMIN),
    USER(RoleName.USER);

    private final String roleName;

    PredefinedRole(String roleName) {
        this.roleName = roleName;
    }

    public String getRoleName() {
        return roleName;
    }

    // Các hằng số static để dùng trong @PreAuthorize
    public static class RoleName {
        public static final String ADMIN = "ADMIN";
        public static final String USER = "USER";
    }
    public static final String HAS_ROLE_ADMIN    = "hasRole('ADMIN')";
    public static final String HAS_ROLE_USER   = "hasRole('USER')";
}