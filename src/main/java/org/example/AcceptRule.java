package org.example;

import java.util.HashMap;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

@DataType()
public class AcceptRule {

    @Property()
    String owner = "";

    @Property()
    HashMap<String, HashMap<String, String>> productToAttributeAndRule = new HashMap<>();
    //(MilchRule, {(quality, gt10), (quantity, lt5)})




    public AcceptRule(){

    }

    public void setProductToAttributeAndRule(HashMap<String, HashMap<String, String>> map){
        productToAttributeAndRule = map;
    }

    public HashMap<String, HashMap<String, String>> getProductToAttributeAndRule() {
        return productToAttributeAndRule;
    }

    public void addEntryToProductToAttributeAndRule(String ruleName, String[] attributeNames, String[] condition){
        HashMap<String, String> attributeToCondition = new HashMap<>();
        for (int i = 0; i < attributeNames.length; i++){
            attributeToCondition.put(attributeNames[i], condition[i]);
        }
        productToAttributeAndRule.put(ruleName, attributeToCondition);
    }

    


    @Override
    public String toString(){
        return "ProductToRule: " + productToAttributeAndRule;
    }

    public String toJSONString() {      
        //return new JSONObject(this).toString();
        Gson gson = new Gson();
        return gson.toJson(this);
        
    }

    public static AcceptRule fromJSONString(String json) {
        AcceptRule acceptRule = new AcceptRule();
        
        String productToAttributeAndRuleString = new JSONObject(json).get("productToAttributeAndRule").toString();
        HashMap<String, HashMap<String, String>>  ProductToRuleMap = new Gson().fromJson(
            productToAttributeAndRuleString, new TypeToken<HashMap<String, HashMap<String, String>> >() {}.getType()
        );
        acceptRule.setProductToAttributeAndRule(ProductToRuleMap);

        return acceptRule;
    
    }


    
}