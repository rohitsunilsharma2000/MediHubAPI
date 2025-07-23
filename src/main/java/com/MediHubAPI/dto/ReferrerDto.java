package com.MediHubAPI.dto;

import lombok.Data;

@Data
public class ReferrerDto {
    private String referrerType;
    private String referrerName;
    private String referrerNumber;
    private String referrerEmail;
    private String consultingDept;
    private String consultingDoctor;
    private String mainComplaint;
}