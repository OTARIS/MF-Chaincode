package org.example;

import com.google.gson.Gson;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

@DataType()
public class PrivateMetaObject {

    @Property()
    String test = "";

    public PrivateMetaObject(){

    }

    public void setTest(String value){
        test = value;
    }

    public String getTest(){
        return test;
    }

    @Override
    public String toString(){
        return "The value is: " +test;
    }
    
    public String toJSONString() {      
        //return new JSONObject(this).toString();
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static PrivateMetaObject fromJSONString(String json) {
        PrivateMetaObject pmo = new PrivateMetaObject();
        String name = new JSONObject(json).getString("test");
        pmo.setTest(name);
        return pmo;
    
    }
}