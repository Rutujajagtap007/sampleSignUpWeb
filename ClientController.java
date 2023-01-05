package com.example.FinalProject.Controller;

import com.example.FinalProject.Module.Client;
import com.example.FinalProject.OTP.CacheRepository;
import com.example.FinalProject.OTP.OtpGenerator;
import com.example.FinalProject.OTP.VerifyOTP;
import com.example.FinalProject.utils.ResponseUtility;
import org.bson.Document;
import org.bson.json.JsonObject;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
public class ClientController {

    @Autowired
    MongoTemplate mt;

    public static final FindAndModifyOptions options = FindAndModifyOptions.options().upsert(true).returnNew(true);
    public static final FindAndModifyOptions modifyOptions = FindAndModifyOptions.options().returnNew(true);

    @Autowired
    CacheRepository cacheRepository;

    @Autowired
    OtpGenerator otpGenerator;

    @Autowired
    ResponseUtility responseUtility;


    @PostMapping("/signUp")
    public String CreateClient(@RequestBody String client) {

        JSONObject clientDetails = new JSONObject(client);
        Query query1 = new Query();
        query1.addCriteria(Criteria.where("mobileNo").is(clientDetails.getString("mobileNo")));
        clientDetails.remove("mobileNo");
        String signUpRes = mt.findOne(query1, String.class, "customerDetails");
        JSONObject mobileNoResponse = new JSONObject(signUpRes);

        if (mobileNoResponse.getBoolean("isOtpVerified")) {
            Update update = new Update();
            update.set("signUpData", Document.parse(clientDetails.toString()));
            mt.findAndModify(query1, update, options, String.class, "customerDetails");
        } else {
            return responseUtility.errorResponse("Mobile no not verified");
        }
        return responseUtility.successResponse("Registration Successfully");
    }


    @PostMapping("/login")
    public String ClientLogin(@RequestBody String login) {

        JSONObject loginDetails = new JSONObject(login);
        Query query =new Query();
        query.addCriteria(Criteria.where("mobileNo").is(loginDetails.getString("mobileNo")));
        String loginRes = mt.findOne(query, String.class, "customerDetails");
        JSONObject loginJSON = new JSONObject(loginRes).getJSONObject("signUpData");

        if (loginJSON.getString("emailId").equals(loginDetails.getString("emailId")) &&
                loginJSON.getString("password").equals(loginDetails.getString("password"))) {
            return responseUtility.successResponse("Login Successfully");
        }
        return responseUtility.errorResponse("Login Failed");
    }


    @PostMapping("/generate-Otp")
    public String addToCache(@RequestBody String key) {

        int value = otpGenerator.generateOtp();
        JSONObject object = new JSONObject(key);
        cacheRepository.put(object.getString("mobileNo"), value);
        return responseUtility.successResponse("Otp Generated Successfully :".concat(String.valueOf(value)));
    }


    @PostMapping("/verify-Otp")
    public String addToCache(@RequestBody VerifyOTP key) {
        JSONObject verifyOtpReqJson = new JSONObject(key);

        if (cacheRepository.get(key.getMobileNo()).isPresent() && cacheRepository.get(key.getMobileNo()).get().equals(key.getOtp())) {
            verifyOtpReqJson.put("isOtpVerified", true);
            verifyOtpReqJson.remove("otp");
            Query query=new Query();
            query.addCriteria(Criteria.where("mobileNo").is(verifyOtpReqJson.getString("mobileNo")));
            System.out.println(query);
            String verifyOptRes = mt.findOne(query, String.class, "customerDetails");
            System.out.println(verifyOptRes);
            if (verifyOptRes != null) {
                Update update = new Update();
                update.set("verifyMobileNo", Document.parse(verifyOtpReqJson.toString()));
                mt.findAndModify(query, update, options, String.class, "customerDetails");
                return responseUtility.successResponse("OTP verified Successfully");
            } else if (verifyOptRes == null) {
                mt.insert(Document.parse(verifyOtpReqJson.toString()), "customerDetails");
                return responseUtility.successResponse("OTP verified Successfully");
            }
        }
        return responseUtility.errorResponse("Invalid OTP!");
    }


}
