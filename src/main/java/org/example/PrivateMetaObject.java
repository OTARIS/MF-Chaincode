package org.example;

import java.util.HashMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

/*
A PrivateMetaObject is connected to a MetaObject and stores the private information
*/

@DataType()
public class PrivateMetaObject {

    @Property()
    HashMap<String, String> attributes = new HashMap<>();
    
    public PrivateMetaObject(){
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    public void addAttribute(String attrName, String attrValue) {
        attributes.put(attrName, attrValue);
    }

    public void deleteAttribute(String attrName) {
        attributes.remove(attrName);
    }
    
    public String toJSONString() {      
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static PrivateMetaObject fromJSONString(String json) {
        PrivateMetaObject pmo = new PrivateMetaObject();
        String attributesString = new JSONObject(json).get("attributes").toString();
        HashMap<String, String> attributesMap = new Gson().fromJson(
            attributesString, new TypeToken<HashMap<String, String>>() {}.getType()
        );      
        pmo.setAttributes(attributesMap);
        return pmo;  
    }
}