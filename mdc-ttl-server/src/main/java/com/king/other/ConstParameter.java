package com.king.other;

import com.king.model.MyPerson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class ConstParameter {


    public static MyPerson person;

    @Autowired
    public void setPerson(MyPerson person){
        ConstParameter.person = person;
    }



}
