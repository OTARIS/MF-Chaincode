package de.metahlfabric;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A PrivateMetaObject is linked to a {@link MetaObject} and stores the private information.
 *
 * @author Tobias Wagner, Dennis Lamken
 *
 * Copyright 2021 OTARIS Interactive Services GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
     * @return the json object
     */
    public JSONObject toJSON() {
        return new JSONObject(this);
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