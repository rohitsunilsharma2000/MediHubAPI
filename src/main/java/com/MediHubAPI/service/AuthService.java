package com.MediHubAPI.service;


import com.MediHubAPI.dto.JwtAuthResponse;
import com.MediHubAPI.dto.LoginDto;
import com.MediHubAPI.dto.RegisterDto;

public interface AuthService {
    JwtAuthResponse login(LoginDto loginDto); // Changed return type from String to JwtAuthResponse
    String register(RegisterDto registerDto);
}