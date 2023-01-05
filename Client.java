package com.example.FinalProject.Module;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.stereotype.Component;
import org.springframework.data.mongodb.core.mapping.Document;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Document(collection = "ClientInformation")

@Component
public class Client {


    @Id

    int id;
    private String fullname;
    private String emailid;
    private String contactNo;
    private String password;
}


