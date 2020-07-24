package org.example;

import java.util.HashMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

/*
AcceptRules can be definied by an organisation to check if a product fulfills the condition
*/

@DataType()
public class AcceptRule {

    @Property()
    String owner = "";

    @Property()
    HashMap<String, HashMap<String, String>> productToAttributeAndRule = new HashMap<>();
    //(milklot, {(quality, gt10), (quantity, lt5)})

    public AcceptRule(){
    }

    public void setProductToAttributeAndRule(HashMap<String, HashMap<String, String>> map){
        productToAttributeAndRule = map;
    }

    public HashMap<String, HashMap<String, String>> getProductToAttributeAndRule() {
        return productToAttributeAndRule;
    }

    public void addEntryToProductToAttributeAndRule(String product, String attributeName, String condition){
        if (productToAttributeAndRule.containsKey(product)){
            HashMap<String, String> attributeAndRule = productToAttributeAndRule.get(product);
            attributeAndRule.put(attributeName, condition);
            productToAttributeAndRule.put(product, attributeAndRule);
        }
        else {
            HashMap<String, String> attributeAndRule = new HashMap<>();
            attributeAndRule.put(attributeName, condition);
            productToAttributeAndRule.put(product, attributeAndRule);
        }
    }

    public void deleteEntryFromProductToAttributeAndRule(String product){
        if (productToAttributeAndRule.containsKey(product)){
            productToAttributeAndRule.remove(product);
        }
    }

    @Override
    public String toString(){
        return toJSONString();
        //return "ProductToRule: " + productToAttributeAndRule;
    }

    public String toJSONString() {      
        Gson gson = new Gson();
        return gson.toJson(this);
      
    }

    public static AcceptRule fromJSONString(String json) {
        AcceptRule acceptRule = new AcceptRule();
        
        String productToAttributeAndRuleString = new JSONObject(json).get("productToAttributeAndRule").toString();
        HashMap<String, HashMap<String, String>>  ProductToRuleMap = new Gson().fromJson(
            productToAttributeAndRuleString, new TypeToken<HashMap<String, HashMap<String, String>>>() {}.getType()
        );
        acceptRule.setProductToAttributeAndRule(ProductToRuleMap);

        return acceptRule;
    }   
}