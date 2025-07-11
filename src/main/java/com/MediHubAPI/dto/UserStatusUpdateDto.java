package com.MediHubAPI.dto;

import lombok.Data;

@Data
public class UserStatusUpdateDto {
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    private boolean enabled;
}
