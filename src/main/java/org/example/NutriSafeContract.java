/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.json.JSONObject;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Contract(name = "NutriSafeContract",
    info = @Info(title = "NutriSafe contract",
                description = "Chaincode for the research project NutriSafe",
                version = "1",
                license =
                        @License(name = "Apache-2.0",
                                url = "https://www.nutrisafe.de"),
                                contact =  @Contact(name = "Tobias Wagner")))

@Default
public class NutriSafeContract implements ContractInterface {

    private String META_DEF_ID = "METADEF";
    private String PDC_STRING = "_P";
    private String ACR_STRING = "_ACR";
    private String AUTHORITY_PDC = "AUTH_PDC";
    
    JSONObject response = new JSONObject(); 

    public  NutriSafeContract() {}

    /* #region utils */

    
    boolean objectExistsIntern(Context ctx, String id){
        byte[] buffer = ctx.getStub().getState(id);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction
    public String objectExists(Context ctx, String id){            
        response.put("status", "200");
        byte[] buffer = ctx.getStub().getState(id);
        response.put("response", (buffer != null && buffer.length > 0));
        return response.toString();

    }

    @Transaction()
    public boolean privateObjectExistsIntern(Context ctx, String id, String pdc) {
        byte[] buffer = ctx.getStub().getPrivateDataHash(pdc, id);        
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public String privateObjectExists(Context ctx, String id, String pdc) {
        response.put("status", "200");
        byte[] buffer = ctx.getStub().getPrivateDataHash(pdc, id);       
        response.put("response", (buffer != null && buffer.length > 0));
        return response.toString();
    }

    @Transaction()
    public String deleteObject(Context ctx, String id){
        boolean exists = objectExistsIntern(ctx, id);
        if (!exists) {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " does not exist");
            return response.toString();
        }
        ctx.getStub().delState(id);
        response.put("status", "200");
        response.put("response", "The object with the key " +id+ " was deleted");
        return response.toString();
    } 

    @Transaction()
    public String deletePrivateObject(Context ctx, String id, String pdc) {
        boolean exists = privateObjectExistsIntern(ctx, id, pdc);
        if (!exists) {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " does not exist in the private data collection " +pdc);
        }
        ctx.getStub().delPrivateData(pdc, id);
        response.put("status", "200");
        response.put("response", "The object with the key " +id+ " was deleted from the private data collection " +pdc);
        return response.toString();
    }

    /* #endregion */

    /* #region META definitions */

    @Transaction()
    public String META_createSampleData(Context ctx){
        MetaDef metaDef = new MetaDef();       
        metaDef.createSampleData();
        ctx.getStub().putState(META_DEF_ID, metaDef.toJSONString().getBytes(UTF_8)); 
        response.put("status", "200");
        response.put("reponse", metaDef.toString());
        return response.toString();   
    }

    @Transaction()
    public String META_readMetaDef(Context ctx){
        if (objectExistsIntern(ctx, META_DEF_ID)){
            MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));
            response.put("status", "200");
            response.put("reponse", metaDef.toString());
            return response.toString();
        }
        else {
            response.put("status", "400");
            response.put("response", "The meta def wiht the key " +META_DEF_ID+ " does not exist");
            return response.toString();
        }
    }

    @Transaction()
    public String META_addAttributeDefinition(Context ctx, String attribute, String dataType){
        if (objectExistsIntern(ctx, META_DEF_ID)){
            MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));
            metaDef.addAttributeDefinition(attribute, dataType);
            ctx.getStub().putState(META_DEF_ID, metaDef.toJSONString().getBytes(UTF_8));  
            response.put("status", "200");
            response.put("reponse", metaDef.toString());
            return response.toString(); 
        }
        else {
            response.put("status", "400");
            response.put("response", "The meta def wiht the key " +META_DEF_ID+ " does not exist");
            return response.toString(); 
        }
    }

    @Transaction()
    public String META_addProductDefinition(Context ctx, String productName, String[] attributes){
        if (objectExistsIntern(ctx, META_DEF_ID)){
            MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));           
            ArrayList<String> attributesArray = new ArrayList<>();
            HashMap<String, String> allowedAttributes = metaDef.getAttributeToDataTypeMap();
            for (int i = 0; i < attributes.length; i++){
                if (allowedAttributes.containsKey(attributes[i])){
                    attributesArray.add(attributes[i]);
                }
                else {
                    response.put("status", "400");
                    response.put("response", "The attribute " +attributes[i]+ " is not defined");
                    return response.toString();                 
                }
            }
            metaDef.addProductDefinition(productName, attributesArray);
            ctx.getStub().putState(META_DEF_ID, metaDef.toJSONString().getBytes(UTF_8));  
            response.put("status", "200");
            response.put("response", metaDef.toString());
            return response.toString();  
        }
        else {
            response.put("status", "400");
            response.put("response", "The meta def wiht the key " +META_DEF_ID+ " does not exist");
            return response.toString(); 
        }
    }

    /* #endregion */

    /* #region META objects */

    @Transaction()
    public String createObject(Context ctx, String id, String pdc, String productName, String[] attributes, String[] attrValues)throws UnsupportedEncodingException{
        if (!objectExistsIntern(ctx, id)){
            MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));             
            if (metaDef.productNameExists(productName)){
                List<String> allowedAttr = metaDef.getAttributesByProductName(productName);
                int i = 0;
                for(String attr : attributes){
                    if (!allowedAttr.contains(attr)) throw new RuntimeException("The attribute " +attr+ " is not defined");
                    if (metaDef.getDataTypeByAttribute(attr) == "Integer"){
                        if (!attrValues[i].matches("-?\\d+")) throw new RuntimeException("The attribute " +attr+ " is not an Integer");
                    }
                i++;
                }
                Map<String, byte[]> transientData = ctx.getStub().getTransient();
                if (transientData.size() != 0) {
                    for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                        if (!allowedAttr.contains(entry.getKey())) throw new RuntimeException("The attribute " +entry.getKey()+ " is not defined");
                    }
                }    
                String timeStamp = ctx.getStub().getTxTimestamp().toString();
                MetaObject metaObject = new MetaObject(pdc, productName, attributes, attrValues, timeStamp, ctx.getClientIdentity().getMSPID());
                ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
                
                PrivateMetaObject privateMetaObject = new PrivateMetaObject();      
                if (!pdc.equals("")) {
                    for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                        privateMetaObject.addAttribute(entry.getKey(), new String(entry.getValue(), "UTF-8"));
                    }
                    ctx.getStub().putPrivateData(pdc, id + PDC_STRING, privateMetaObject.toJSONString().getBytes(UTF_8));
                }
                response.put("status", "200");
                response.put("response", metaObject.toString() + "Private Data: \n" + privateMetaObject.toString());
                return response.toString();      
            }
            else{
                response.put("status", "400");
                response.put("response", "The product name" +productName+ " is not defined");
                return response.toString();
            }
        }
        else {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " already exists");
            return response.toString();
        }       
    }

    @Transaction()
    public String readObject(Context ctx, String id){
        if (objectExistsIntern(ctx, id)){
            String pmoString = "";
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (!metaObject.getPrivateDataCollection().equals("")){
                try {
                    byte[] privateMetaObject = ctx.getStub().getPrivateData(metaObject.getPrivateDataCollection(), id + PDC_STRING);
                    pmoString = new String(privateMetaObject, "UTF-8");                  
                }
                catch (Exception e){}   
            }          
            response.put("status", "200");
            response.put("response", metaObject.toString() + "Private Data: \n" + pmoString);
            return response.toString();
        }      
        else {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " does not exist");
            return response.toString();
        }
    }

    @Transaction()
    public String setReceiver(Context ctx, String id, String receiver, String pdcOfACRRule) throws UnsupportedEncodingException{
        HashMap<String, String> attributesToCheck = new HashMap<>();
        String returnV = "";
        if (objectExistsIntern(ctx, id)){
            
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (metaObject.getPrivateDataCollection().length() >= 3){
                byte[] pmoArray = ctx.getStub().getPrivateData(metaObject.getPrivateDataCollection(), id + PDC_STRING);
                String pmoString = new String(pmoArray, "UTF-8");  
                PrivateMetaObject privateMetaObject = PrivateMetaObject.fromJSONString(pmoString);
                attributesToCheck.putAll(privateMetaObject.getAttributes());
            }
            if (metaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID())){
                if (privateObjectExistsIntern(ctx, receiver + ACR_STRING, pdcOfACRRule)){  //AcceptRule definiton must be in the same PDC as the PrivateMetaObject
                    AcceptRule acceptRule = readAccept(ctx, receiver, metaObject.getPrivateDataCollection());
                    HashMap<String, HashMap<String, String>> acceptRules = acceptRule.getProductToAttributeAndRule();
                    if (acceptRules.containsKey(metaObject.getProductName())){
                        HashMap<String, String> attributeToCondition = acceptRules.get(metaObject.getProductName());
                        attributesToCheck.putAll(metaObject.getAttributes());

                        for (Map.Entry<String, String> entry : attributeToCondition.entrySet()){
                            String condition = entry.getValue();
                            String operator = condition.substring(0,2);  //eq, lt, gt
                            condition = condition.substring(2, condition.length());                               
                            if (operator.equals("eq")){
                                returnV += "eq";
                                if (!attributesToCheck.get(entry.getKey()).equals(condition)) throw new RuntimeException("The attribute " +entry.getKey()+ " with the value " +attributesToCheck.get(entry.getKey())+ " does not match the condition " + condition);                               
                            }
                            else if (operator.equals("lt")){
                                returnV += "lt";
                                 if (Integer.parseInt(attributesToCheck.get(entry.getKey())) >= Integer.parseInt(condition)) throw new RuntimeException ("The attribute " +entry.getKey()+ " with the value " +attributesToCheck.get(entry.getKey())+ " is not lower than " + condition);
                            }
                            else if (operator.equals("gt")){
                                returnV += "gt";
                                  if (Integer.parseInt(attributesToCheck.get(entry.getKey())) <= Integer.parseInt(condition)) throw new RuntimeException ("The attribute " +entry.getKey()+ " with the value " +attributesToCheck.get(entry.getKey())+ " is not greater than " + condition);                                                                                   
                            }
                        }                                            
                    }                                      
                }
                metaObject.setReceiver(receiver);                  
                ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8)); 
                response.put("status", "200");
                response.put("response", metaObject.toString());
                return response.toString();          
            }
            else {
                response.put("status", "400");
                response.put("response", "You (" + ctx.getClientIdentity().getMSPID() + ") are not the actual owner");
                return response.toString();
            }
        }
        else {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " does not exist");
            return response.toString();
        }
        
    }

    @Transaction()
    public String changeOwner(Context ctx, String id){
        if (objectExistsIntern(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (metaObject.getReceiver().equals(ctx.getClientIdentity().getMSPID())){
                metaObject.setReceiver("");
                metaObject.addTsAndOwner(ctx.getStub().getTxTimestamp().toString(), ctx.getClientIdentity().getMSPID());
                metaObject.setActualOwner(ctx.getClientIdentity().getMSPID());
                ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
                response.put("status", "200");
                response.put("response", metaObject.toString());
                return response.toString();
            }
            else {
                response.put("status", "400");
                response.put("response", "You (" + ctx.getClientIdentity().getMSPID() + ") are not the receiver");
                return response.toString();
            }
        }
        else {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " does not exist");
            return response.toString();
        }
    }

    @Transaction()
    public String addPredecessor(Context ctx, String[] predecessorIds, String id){
        if (objectExistsIntern(ctx, id)){
            for (String preId : predecessorIds){
                response.put("status", "400");
                response.put("response", "The object with the key " +preId+ " does not exist");
                return response.toString();
            }
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (!metaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID())) throw new RuntimeException("You are not the owner of " +id);
            ArrayList<MetaObject> predecessors = new ArrayList<>();
            for (String preId : predecessorIds){
                predecessors.add(MetaObject.fromJSONString(new String(ctx.getStub().getState(preId))));
            }
            for (MetaObject preMetaObject : predecessors){
                if (!preMetaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID())) throw new RuntimeException("You are not the owner of at least one predecessor");
            }      
            metaObject.addPredecessor(predecessorIds);
            ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));

            int i = 0;
            for (MetaObject preMetaObject : predecessors){
                preMetaObject.addSuccessor(id);
                ctx.getStub().putState(predecessorIds[i], preMetaObject.toJSONString().getBytes(UTF_8));
                i++;
            }    
            response.put("status", "200");
            response.put("response", metaObject.toString();
            return response.toString();   
        }
        else {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " does not exist");
            return response.toString();
        }
    }

    @Transaction()
    public String updateAttribute(Context ctx, String id, String attrName, String attrValue){
        //TODO Datetyp prüfen
        if (objectExistsIntern(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));   
            List<String> allowedAttr = metaDef.getAttributesByProductName(metaObject.getProductName());
            if (attrName != ""){
                if (allowedAttr.contains(attrName)){
                    metaObject.addAttribute(attrName, attrValue);
                    ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
                }
                else {
                    response.put("status", "400");
                    response.put("response", "The attrName "+attrName+  " is not defined");
                    return response.toString();
                }  
            }
 
            Map<String, byte[]> transientData = ctx.getStub().getTransient();
            if (transientData.size() != 0) {
                for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                    if (!allowedAttr.contains(entry.getKey())) {
                        response.put("status", "400");
                        response.put("response", "The attrName "+entry.getKey()+  " is not defined");
                        return response.toString();
                    }
                }
            }
            PrivateMetaObject privateMetaObject = new PrivateMetaObject();
            if (metaObject.getPrivateDataCollection().length() > 2){
                try {
                    byte[] pmoArray = ctx.getStub().getPrivateData(metaObject.getPrivateDataCollection(), id + PDC_STRING);
                    String pmoString = new String(pmoArray, "UTF-8");
                    privateMetaObject = PrivateMetaObject.fromJSONString(pmoString);
                    
                    for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                        privateMetaObject.addAttribute(entry.getKey(), new String(entry.getValue(), "UTF-8"));
                    }
                    ctx.getStub().putPrivateData(metaObject.getPrivateDataCollection(), id + PDC_STRING, privateMetaObject.toJSONString().getBytes(UTF_8));
                }                
                catch (Exception e){
                    response.put("status", "400");
                    response.put("response", "You are not allowed to change de PDC");
                    return response.toString();
                }    
            } 
            response.put("status", "200");
            response.put("response", metaObject.toString() + "Private Data: \n" + privateMetaObject.toString());
            return response.toString();     
        }
        else {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " does not exist");
            return response.toString();
        }
    } 
    
    /* #endregion */

    /* #region Accept rules */

    @Transaction()
    public String addRuleNameAndCondition(Context ctx, String pdc, String product) throws UnsupportedEncodingException{
        AcceptRule acceptRule = new AcceptRule();
        String acrKey = ctx.getClientIdentity().getMSPID() + ACR_STRING;
        if (privateObjectExistsIntern(ctx, acrKey, pdc)){    
            byte[] acc = ctx.getStub().getPrivateData(pdc, acrKey);
            String accString = new String(acc, "UTF-8");
            acceptRule = AcceptRule.fromJSONString(accString);     
        }
        Map<String, byte[]> transientData = ctx.getStub().getTransient();   
        if (transientData.size() != 0) {
            for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                acceptRule.addEntryToProductToAttributeAndRule(product, entry.getKey(), new String(entry.getValue(), "UTF-8"));
            }
            ctx.getStub().putPrivateData(pdc, acrKey, acceptRule.toJSONString().getBytes(UTF_8));
            response.put("status", "200");
            response.put("response",acceptRule.toString());
            return response.toString();
            
        } 
        else {
            response.put("status", "400");
            response.put("response","No transient data passed");
            return response.toString();
        }
    }

    //not tested
    @Transaction()
    public String deleteRuleForProduct(Context ctx, String pdc, String product) throws UnsupportedEncodingException{
        String acrKey = ctx.getClientIdentity().getMSPID() + ACR_STRING;
        
        if (privateObjectExistsIntern(ctx, acrKey, pdc)){ 
            byte[] acc = ctx.getStub().getPrivateData(pdc, acrKey);
            String accString = new String(acc, "UTF-8");
            AcceptRule acceptRule = AcceptRule.fromJSONString(accString); 
            acceptRule.deleteEntryFromProductToAttributeAndRule(product);
            ctx.getStub().putPrivateData(pdc, acrKey, acceptRule.toJSONString().getBytes(UTF_8));
            response.put("status", "200");
            response.put("response",acceptRule.toString());
            return response.toString();
        }
        else {
            response.put("status", "400");
            response.put("response", "There is no AcceptRule Object defined");
            return response.toString();
        }

    }

    @Transaction()
    public String readAccept(Context ctx, String id, String pdc) throws UnsupportedEncodingException{
        if (privateObjectExistsIntern(ctx, id + ACR_STRING, pdc)){ 
            byte[] acc = ctx.getStub().getPrivateData(pdc, id + ACR_STRING);
            String accString = new String(acc, "UTF-8");
            AcceptRule acr = AcceptRule.fromJSONString(accString);      
            response.put("status", "200");
            response.put("response", acr.toString());
            return response.toString();
            
        }
        else {
            response.put("status", "400");
            response.put("response", "There is no AcceptRule Object defined");
            return response.toString();
        }
    }

    /* #endregion */
    
    /* #region alarm handling */

    //not tested
    @Transaction()
    public String activateAlarm(Context ctx, String id){
        //TODO Prüfung auf Berechtigung
        if (objectExistsIntern(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            metaObject.setAlarmFlag(true);
            ArrayList<String> successors = metaObject.getSuccessor();
            for (String suc : successors){
                MetaObject sucMetaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(suc)));
                sucMetaObject.setAlarmFlag(true);
            }
            response.put("status", "400");
            response.put("response", metaObject.toString());
            return response.toString();
        }
        else {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " does not exist");
            return response.toString();
        }
    }

    //not tested
    @Transaction()
    public String exportDataToAuthPDC(Context ctx, String id) throws UnsupportedEncodingException{
        if (objectExistsIntern(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            HashMap<String, String> attributes = new HashMap<>();
            if (metaObject.getAlarmFlag() == true){
                if (metaObject.getPrivateDataCollection().length() > 2){
                    byte[] pmoArray = ctx.getStub().getPrivateData(metaObject.getPrivateDataCollection(), id + PDC_STRING);
                    String pmoString = new String(pmoArray, "UTF-8");
                    PrivateMetaObject privateMetaObject = PrivateMetaObject.fromJSONString(pmoString); 
                    attributes.putAll(privateMetaObject.getAttributes());
                }  
                metaObject.addAllAttributes(attributes);
                ctx.getStub().putPrivateData(AUTHORITY_PDC, id,metaObject.toJSONString().getBytes(UTF_8));
                response.put("status", "200");
                response.put("response", metaObject.toString());
                return response.toString();
                
            }
            else {
                response.put("status", "400");
                response.put("response", "The alarm flag for " +id+  "is set to false");
                return response.toString();
            }
        }
        else {
            response.put("status", "400");
            response.put("response", "The object with the key " +id+ " does not exist");
            return response.toString();
        }
    }

    /* #endregion */
}

