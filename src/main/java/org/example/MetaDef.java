package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

/*
The MetaDef defines the attributes and products available in this channel
*/

@DataType()
public class MetaDef {

    @Property()
    HashMap<String, List<String>> productNameToAttributesMap;

    @Property()
    HashMap<String, String> attributeToDataTypeMap;

    public MetaDef(){
        productNameToAttributesMap = new HashMap<>();
        attributeToDataTypeMap = new HashMap<>();
    }

    public void addProductDefinition(String productName, List<String> attributes){
        productNameToAttributesMap.put(productName, attributes);
    }

    public void addAttributeDefinition(String attribute, String dataType){
        attributeToDataTypeMap.put(attribute, dataType);
    }

    public void setProductNameToAttributesMap(HashMap<String, List<String>> map){
        productNameToAttributesMap = map;
    }

    public HashMap<String, List<String>> getProductNameToAttributesMap(){
        return productNameToAttributesMap;
    }

    public List<String> getAttributesByProductName(String productName) {
        return productNameToAttributesMap.get(productName);
    }

    public boolean productNameExists(String productName){
        return productNameToAttributesMap.containsKey(productName);
    }

    public void setAttributeToDataTypeMap(HashMap<String, String> map){
        attributeToDataTypeMap = map;
    }

    public HashMap<String, String> getAttributeToDataTypeMap(){
        return attributeToDataTypeMap;
    }

    public String getDataTypeByAttribute(String attribute){
        return attributeToDataTypeMap.get(attribute);
    }

    public boolean attributeExists(String attribute){
        return attributeToDataTypeMap.containsKey(attribute);
    }

    public void createSampleData(){
        ArrayList<String> sample = new ArrayList<>();
        sample.add("Quality");
        sample.add("AmountInLiter");
        productNameToAttributesMap.put("milklot", sample);
        attributeToDataTypeMap.put("Quality", "String");
        attributeToDataTypeMap.put("AmountInLiter", "Integer");
    }

    @Override
    public String toString(){
        return toJSONString();
        /*
        StringBuilder sb = new StringBuilder();
        sb.append("Product name to attributes:" + productNameToAttributesMap.toString() + "\n");
        sb.append("Attributes to data type:" + attributeToDataTypeMap.toString());
        return sb.toString();
        */
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public static MetaDef fromJSONString(String json){
        MetaDef metaDef = new MetaDef();

        String productString = new JSONObject(json).get("productNameToAttributesMap").toString();
        HashMap<String, List<String>> productMap = new Gson().fromJson(
            productString, new TypeToken<HashMap<String, List<String>>>() {}.getType()
        );
        metaDef.setProductNameToAttributesMap(productMap);

        String attributeString = new JSONObject(json).get("attributeToDataTypeMap").toString();
        HashMap<String, String> attributeMap = new Gson().fromJson(
            attributeString, new TypeToken<HashMap<String, String>>() {}.getType()
        );
        metaDef.setAttributeToDataTypeMap(attributeMap);

        return metaDef;     
    }   
}