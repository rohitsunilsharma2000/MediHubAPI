package com.MediHubAPI.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import jakarta.annotation.security.RolesAllowed;
import org.springframework.web.bind.annotation.*;

// @CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/test-roles")
public class RoleTestController {

    @GetMapping("/super-admin")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public String superAdminOnly() {
        return " SUPER_ADMIN can access this endpoint";
    }

    @GetMapping("/admin-or-hr")
    @PreAuthorize("hasAnyRole('ADMIN','HR_MANAGER')")
    public String adminOrHrAccess() {
        return "ADMIN or HR_MANAGER can access this endpoint";
    }

    @GetMapping("/billing")
    @Secured("ROLE_BILLING_CLERK")
    public String billingAccess() {
        return "BILLING_CLERK can access this endpoint";
    }

    @GetMapping("/pharmacist-or-doctor")
    @RolesAllowed({"ROLE_PHARMACIST", "ROLE_DOCTOR"})
    public String pharmacistOrDoctor() {
        return "PHARMACIST or DOCTOR can access this endpoint";
    }

    @GetMapping("/debug")
    public String debugRoles(org.springframework.security.core.Authentication auth) {
        return "🛡️ Your Roles: " + auth.getAuthorities();
    }


}
