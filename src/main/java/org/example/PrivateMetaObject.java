package org.example;

import java.util.HashMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

/**
 * A PrivateMetaObject is linked to a MetaObject and stores the private information
 */

@DataType()
public class PrivateMetaObject {

    /**
     * The private attributes to store
     */
    @Property()
    HashMap<String, String> attributes = new HashMap<>();
    
    /**
     * Class constructor
     */
    public PrivateMetaObject(){
    }

    /**
     * @return the map of private attributes
     */
    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    /**
     * @param attributes the HashMap of attributes to set
     */
    public void setAttributes(HashMap<String, String> attributes) {
        this.attributes = attributes;
    }

    /**
     * @param attrName the name of the attribute to add
     * @param attrValue the value of the attribute to add
     */
    public void addAttribute(String attrName, String attrValue) {
        attributes.put(attrName, attrValue);
    }

    /**
     * @param attrName the name of the attribute to delete
     */
    public void deleteAttribute(String attrName) {
        attributes.remove(attrName);
    }

    /**
     * @return the object as a json string
     */
    @Override
    public String toString(){
        return toJSONString();
    }
    
    /**
     * @return the object as a json string
     */
    public String toJSONString() {      
        Gson gson = new Gson();
        return gson.toJson(this);
    }

     /**
     * Converts the json string of this object back to a PrivateMetaObject
     * 
     * @param json the json String of the object to decrypt
     * @return the decrypted object
     */
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