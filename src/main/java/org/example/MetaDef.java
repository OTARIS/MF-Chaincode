package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

/**
 * The MetaDef defines the attributes and products available in this channel
 */

@DataType()
public class MetaDef {

    /**
     * HashMap which assigns attributes to products
     */
    @Property()
    HashMap<String, List<String>> productNameToAttributesMap;

    /**
     * HashMap which assigns attributes to data types
     */
    @Property()
    HashMap<String, String> attributeToDataTypeMap;

    /**
     * List of defined units
     */
    @Property()
    ArrayList<String> unitList;

    /**
     * Class constructor
     */
    public MetaDef(){
        productNameToAttributesMap = new HashMap<>();
        attributeToDataTypeMap = new HashMap<>();
        unitList = new ArrayList<>();
    }

    /** 
     * @return the list of defined units
     */
    public ArrayList<String> getUnitList(){
        return unitList;
    }

    /**
     * @param unitList list of units to set
     */
    public void setUnitList(ArrayList<String> unitList){
        this.unitList = unitList;
    }

    /**
     * @param unit the unit definition to add
     */
    public void addUnitToUnitList(String unit){
        unitList.add(unit);
    }

    /**
     * @return the hash map of defined attributes
     */
    public HashMap<String, String> getAttributeToDataTypeMap(){
        return attributeToDataTypeMap;
    }

    /**
     * @param attribute get data type for this attribute
     * @return the data typ for the specified attribute
     */
    public String getDataTypeByAttribute(String attribute){
        return attributeToDataTypeMap.get(attribute);
    }

    /**
     * @param map HashMap of attributes to set
     */
    public void setAttributeToDataTypeMap(HashMap<String, String> map){
        attributeToDataTypeMap = map;
    }

    /**
     * @param attribute the attribute to add
     * @param dataType the data type of the attribute to add
     */
    public void addAttributeDefinition(String attribute, String dataType){
        attributeToDataTypeMap.put(attribute, dataType);
    }

    /**
     * @param attribute the attribute to check
     * @return true if the atrribute exits
     */
    public boolean attributeExists(String attribute){
        return attributeToDataTypeMap.containsKey(attribute);
    }

    /**
     * @return the HashMap of defined products
     */
    public HashMap<String, List<String>> getProductNameToAttributesMap(){
        return productNameToAttributesMap;
    }

    /**
     * @param productName get attributes of this product
     * @return the attributes of the specified product
     */
    public List<String> getAttributesByProductName(String productName) {
        return productNameToAttributesMap.get(productName);
    }

    /**
     * @param map the HashMap of products to set
     */
    public void setProductNameToAttributesMap(HashMap<String, List<String>> map){
        productNameToAttributesMap = map;
    }

    /**
     * @param productName the product to add
     * @param attributes the attributes to add
     */
    public void addProductDefinition(String productName, List<String> attributes){
        productNameToAttributesMap.put(productName, attributes);
    }

    /**
     * @param productName the product to check
     * @return true if the product exists
     */
    public boolean productNameExists(String productName){
        return productNameToAttributesMap.containsKey(productName);
    }

    /**
     * @return the object as a json string
     */
    public String toString(){
        return toJSONString();
    }

    /**
     * @return the object as a json string
     */
    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    /**
     * Converts the json string of this object back to a MetaDef
     * 
     * @param json the json String of the object to decrypt
     * @return the decrypted object
     */
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