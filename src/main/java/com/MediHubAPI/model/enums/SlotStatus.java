package com.MediHubAPI.model.enums;



public enum SlotStatus {
    UNAVAILABLE("#E5E5EA"),
    AVAILABLE("#00B386"),
    BOOKED("#F28C28"),
    ADDITIONAL("#FF5E57"),
    ARRIVED("#EB4D9C"),
    COMPLETED("#A069E5"),
    WALKIN("#FBC02D"),
    BLOCKED("#2F2F38"),
    NO_SHOW("#D32F2F"),
    RESERVED("#4285F4");

    private final String colorCode;

    SlotStatus(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColorCode() {
        return colorCode;
    }
}