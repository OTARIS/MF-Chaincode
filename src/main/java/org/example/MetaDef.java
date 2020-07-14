package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

@DataType()
public class MetaDef {

    @Property()
    HashMap<String, List<String>> dataNameToFieldMap;

    @Property()
    HashMap<String, String> fieldToTypeMap;

    public MetaDef(){
        dataNameToFieldMap = new HashMap<>();
        fieldToTypeMap = new HashMap<>();
    }

    public void addDataName(String dataName, List<String> fieldNames){
        dataNameToFieldMap.put(dataName, fieldNames);
    }

    public void addFieldType(String fieldName, String dataType){
        fieldToTypeMap.put(fieldName, dataType);
    }

    public void setDataNameToFieldMap(HashMap<String, List<String>> map){
        dataNameToFieldMap = map;
    }

    public void setFieldToTypeMap(HashMap<String, String> map){
        fieldToTypeMap = map;
    }

    public HashMap<String, List<String>> getDataNameToFieldMap(){
        return dataNameToFieldMap;
    }

    public HashMap<String, String> getFieldToTypeMap(){
        return fieldToTypeMap;
    }

    public List<String> getFieldsByDataName(String dataName) {
        return dataNameToFieldMap.get(dataName);
    }

    public String getDataTypeByFieldName(String fieldName){
        return fieldToTypeMap.get(fieldName);
    }

    public boolean dataNameExists(String dataName){
        return dataNameToFieldMap.containsKey(dataName);
    }

    public boolean fieldExists(String fieldName){
        return fieldToTypeMap.containsKey(fieldName);
    }

    public void createSampleData(){
        ArrayList<String> sample = new ArrayList<>();
        sample.add("Quality");
        sample.add("AmountInLiter");
        dataNameToFieldMap.put("milklot", sample);
        fieldToTypeMap.put("Quality", "String");
        fieldToTypeMap.put("AmountInLiter", "Integer");
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public static MetaDef fromJSONString(String json){
        MetaDef metaDef = new MetaDef();

        String dataNameString = new JSONObject(json).get("dataNameToFieldMap").toString();
        HashMap<String, List<String>> dataNameMap = new Gson().fromJson(
            dataNameString, new TypeToken<HashMap<String, List<String>>>() {}.getType()
        );
        metaDef.setDataNameToFieldMap(dataNameMap);

        String fieldTypeString = new JSONObject(json).get("fieldToTypeMap").toString();
        HashMap<String, String> fieldTypeMap = new Gson().fromJson(
            fieldTypeString, new TypeToken<HashMap<String, String>>() {}.getType()
        );
        metaDef.setFieldToTypeMap(fieldTypeMap);

        return metaDef;
        
       
        
    }

    
}