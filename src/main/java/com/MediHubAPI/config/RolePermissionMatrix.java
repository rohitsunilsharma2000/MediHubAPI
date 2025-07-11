package com.MediHubAPI.config;



import com.MediHubAPI.model.ERole;

import java.util.*;

public class RolePermissionMatrix {

    private static final Map<ERole, Set<ERole>> permissionMap = new HashMap<>();

    static {
        permissionMap.put(ERole.SUPER_ADMIN, EnumSet.of(
                ERole.ADMIN, ERole.DOCTOR, ERole.NURSE,
                ERole.RECEPTIONIST, ERole.BILLING_CLERK,
                ERole.PHARMACIST, ERole.HR_MANAGER
        ));

        permissionMap.put(ERole.ADMIN, EnumSet.of(
                ERole.DOCTOR, ERole.NURSE, ERole.RECEPTIONIST,
                ERole.BILLING_CLERK, ERole.PHARMACIST
        ));

        permissionMap.put(ERole.HR_MANAGER, EnumSet.of(
                ERole.NURSE, ERole.RECEPTIONIST, ERole.BILLING_CLERK
        ));
    }

    public static boolean canCreate(ERole creator, ERole toCreate) {
        return permissionMap.containsKey(creator) &&
                permissionMap.get(creator).contains(toCreate);
    }

    public static boolean isRecognizedRole(ERole role) {
        return EnumSet.allOf(ERole.class).contains(role);
    }
}

