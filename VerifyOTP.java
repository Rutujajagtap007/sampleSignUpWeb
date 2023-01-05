package com.example.FinalProject.OTP;

import lombok.Getter;

import java.io.Serializable;

@Getter
public class VerifyOTP implements Serializable {


    private String mobileNo;
    private String otp;
}
