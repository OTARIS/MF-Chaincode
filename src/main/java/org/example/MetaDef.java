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

    @Property()
    ArrayList<String> unitList;

    public MetaDef(){
        productNameToAttributesMap = new HashMap<>();
        attributeToDataTypeMap = new HashMap<>();
        unitList = new ArrayList<>();
    }

    //UnitList
    public ArrayList<String> getUnitList(){
        return unitList;
    }

    public void setUnitList(ArrayList<String> unitList){
        this.unitList = unitList;
    }

    public void addUnitToUnitList(String unit){
        unitList.add(unit);
    }

    //Attribute definitions
    public HashMap<String, String> getAttributeToDataTypeMap(){
        return attributeToDataTypeMap;
    }

    public String getDataTypeByAttribute(String attribute){
        return attributeToDataTypeMap.get(attribute);
    }

    public void setAttributeToDataTypeMap(HashMap<String, String> map){
        attributeToDataTypeMap = map;
    }

    public void addAttributeDefinition(String attribute, String dataType){
        attributeToDataTypeMap.put(attribute, dataType);
    }

    public boolean attributeExists(String attribute){
        return attributeToDataTypeMap.containsKey(attribute);
    }

    //Product definitions
    public HashMap<String, List<String>> getProductNameToAttributesMap(){
        return productNameToAttributesMap;
    }

    public List<String> getAttributesByProductName(String productName) {
        return productNameToAttributesMap.get(productName);
    }

    public void setProductNameToAttributesMap(HashMap<String, List<String>> map){
        productNameToAttributesMap = map;
    }

    public void addProductDefinition(String productName, List<String> attributes){
        productNameToAttributesMap.put(productName, attributes);
    }

    public boolean productNameExists(String productName){
        return productNameToAttributesMap.containsKey(productName);
    }

    public String toString(){
        return toJSONString();
    }

    //Save and load
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

        String unitString = new JSONObject(json).get("unitList").toString();
        ArrayList<String> unitList = new Gson().fromJson(
            unitString, new TypeToken<ArrayList<String>>() {}.getType()
        );
        metaDef.setUnitList(unitList);

        return metaDef;     
    }   
}