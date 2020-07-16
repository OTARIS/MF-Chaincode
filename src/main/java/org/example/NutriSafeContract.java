/*
 * SPDX-License-Identifier: Apache-2.0
 */
package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
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

    public  NutriSafeContract() {}

    /* #region utils */

    @Transaction()
    public boolean objectExists(Context ctx, String id){
        byte[] buffer = ctx.getStub().getState(id);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public boolean privateObjectExists(Context ctx, String id, String pdc) {
        byte[] buffer = ctx.getStub().getPrivateDataHash(pdc, id);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void deleteObject(Context ctx, String id){
        boolean exists = objectExists(ctx, id);
        if (!exists) {
            throw new RuntimeException("The object does not exist");
        }
        ctx.getStub().delState(id);
    } 

    @Transaction()
    public void deletePrivateObject(Context ctx, String id, String pdc) {
        boolean exists = privateObjectExists(ctx, id, pdc);
        if (!exists) {
            throw new RuntimeException("The private asset " +id+ " in the collection " +pdc+ " does not exist");
        }
        ctx.getStub().delPrivateData(pdc, id);
    }

    /* #endregion */

    /* #region META definitions */

    @Transaction()
    public String META_createSampleData(Context ctx){
        MetaDef metaDef = new MetaDef();
        metaDef.createSampleData();
        ctx.getStub().putState(META_DEF_ID, metaDef.toJSONString().getBytes(UTF_8)); 
        return metaDef.toString();    
    }

    @Transaction()
    public MetaDef META_readMetaDef(Context ctx){
        if (objectExists(ctx, META_DEF_ID)){
            MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));
            return metaDef;
        }
        else {
            throw new RuntimeException("The MetaDef does not exist");
        }
    }

    @Transaction()
    public void META_addAttributeDefinition(Context ctx, String attribute, String dataType){
        if (objectExists(ctx, META_DEF_ID)){
            MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));
            metaDef.addAttributeDefinition(attribute, dataType);
            ctx.getStub().putState(META_DEF_ID, metaDef.toJSONString().getBytes(UTF_8));         
        }
    }

    @Transaction()
    public void META_addProductDefinition(Context ctx, String productName, String[] attributes){
        if (objectExists(ctx, META_DEF_ID)){
            MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));           
            ArrayList<String> attributesArray = new ArrayList<>();
            HashMap<String, String> allowedAttributes = metaDef.getAttributeToDataTypeMap();
            for (int i = 0; i < attributes.length; i++){
                if (allowedAttributes.containsKey(attributes[i])){
                    attributesArray.add(attributes[i]);
                }
                else {
                    throw new RuntimeException("The attribute " +attributes[i] + " is not defined");
                    
                }
            }
            metaDef.addProductDefinition(productName, attributesArray);
            ctx.getStub().putState(META_DEF_ID, metaDef.toJSONString().getBytes(UTF_8));    
        }  
    }

    /* #endregion */

    /* #region META objects */

    @Transaction()
    public void createObject(Context ctx, String id, String pdc, String productName, String[] attributes, String[] attrValues)throws UnsupportedEncodingException{
        if (!objectExists(ctx, id)){
            MetaDef metaDef = META_readMetaDef(ctx);           
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
            }
            else{
                throw new RuntimeException("The product name" +productName+ " is not defined");
            }
        }
        else {
            throw new RuntimeException("The ID "+id+" already exists");
        }       
    }

    @Transaction()
    public String readObject(Context ctx, String id){
        if (objectExists(ctx, id)){
            String pmoString = "";
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (!metaObject.getPrivateDataCollection().equals("")){
                try {
                    byte[] privateMetaObject = ctx.getStub().getPrivateData(metaObject.getPrivateDataCollection(), id + PDC_STRING);
                    pmoString = new String(privateMetaObject, "UTF-8");                  
                }
                catch (Exception e){}   
            }
            String result = metaObject.toString() + "Private Data: \n" + pmoString;
            return result;
        }      
        else {
            throw new RuntimeException("The ID "+id+" does not exist");
        }
    }

    @Transaction()
    public String setReceiver(Context ctx, String id, String receiver, String pdcOfACRRule) throws UnsupportedEncodingException{
        HashMap<String, String> attributesToCheck = new HashMap<>();
        String returnV = "";
        if (objectExists(ctx, id)){
            
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (metaObject.getPrivateDataCollection().length() >= 3){
                byte[] pmoArray = ctx.getStub().getPrivateData(metaObject.getPrivateDataCollection(), id + PDC_STRING);
                String pmoString = new String(pmoArray, "UTF-8");  
                PrivateMetaObject privateMetaObject = PrivateMetaObject.fromJSONString(pmoString);
                attributesToCheck.putAll(privateMetaObject.getAttributes());
            }
            if (metaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID())){
                if (privateObjectExists(ctx, receiver + ACR_STRING, pdcOfACRRule)){  //AcceptRule definiton must be in the same PDC as the PrivateMetaObject
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
            }
            else {
                throw new RuntimeException("You (" + ctx.getClientIdentity().getMSPID() + ") are not the actual owner");
            }
        }
        else {
            throw new RuntimeException("The ID "+id+" does not exist");
        }
        return returnV;
    }

    @Transaction()
    public void changeOwner(Context ctx, String id){
        if (objectExists(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (metaObject.getReceiver().equals(ctx.getClientIdentity().getMSPID())){
                metaObject.setReceiver("");
                metaObject.addTsAndOwner(ctx.getStub().getTxTimestamp().toString(), ctx.getClientIdentity().getMSPID());
                metaObject.setActualOwner(ctx.getClientIdentity().getMSPID());
                ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
            }
            else {
                throw new RuntimeException("You (" + ctx.getClientIdentity().getMSPID() + ") are not the receiver");
            }
        }
        else {
            throw new RuntimeException("The ID "+id+" does not exist");
        }
    }

    @Transaction()
    public void addPredecessor(Context ctx, String[] predecessorIds, String id){
        if (objectExists(ctx, id)){
            for (String preId : predecessorIds){
                if (!objectExists(ctx, preId)) throw new RuntimeException("The ID "+preId+" does not exist");
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
        }
        else throw new RuntimeException("The ID "+id+" does not exist");
    }

    @Transaction()
    public void updateAttribute(Context ctx, String id, String attrName, String attrValue){
        //TODO update private data
        //TODO Datetyp prüfen
        if (objectExists(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            MetaDef metaDef = META_readMetaDef(ctx);
            List<String> allowedAttr = metaDef.getAttributesByProductName(metaObject.getProductName());
            if (attrName != ""){
                if (allowedAttr.contains(attrName)){
                    metaObject.addAttribute(attrName, attrValue);
                    ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
                }
                else {
                    throw new RuntimeException("The attrName "+attrName+  " is not defined");
                }  
            }
 
            Map<String, byte[]> transientData = ctx.getStub().getTransient();
            if (transientData.size() != 0) {
                for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                    if (!allowedAttr.contains(entry.getKey())) throw new RuntimeException("The attrName "+entry.getKey()+  " is not defined");
                }
            }
            if (metaObject.getPrivateDataCollection().length() > 2){
                try {
                    byte[] pmoArray = ctx.getStub().getPrivateData(metaObject.getPrivateDataCollection(), id + PDC_STRING);
                    String pmoString = new String(pmoArray, "UTF-8");
                    PrivateMetaObject privateMetaObject = PrivateMetaObject.fromJSONString(pmoString);
                    
                    for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                        privateMetaObject.addAttribute(entry.getKey(), new String(entry.getValue(), "UTF-8"));
                    }
                    ctx.getStub().putPrivateData(metaObject.getPrivateDataCollection(), id + PDC_STRING, privateMetaObject.toJSONString().getBytes(UTF_8));
                }                
                catch (Exception e){
                    throw new RuntimeException("You are not allowed to change de PDC");
                }    
            }      
        }
        else {
            throw new RuntimeException("The ID "+id+" does not exist");
        }
    } 
    
    /* #endregion */

/* #region Accept rules */

    @Transaction()
    public void addRuleNameAndCondition(Context ctx, String product, String pdc) throws UnsupportedEncodingException{
        AcceptRule acceptRule = new AcceptRule();
        String acrKey = ctx.getClientIdentity().getMSPID() + ACR_STRING;
        if (privateObjectExists(ctx, acrKey, pdc)){    
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
            
        } 
        else {
            throw new RuntimeException("No transient data passed");
        }
    }

    //not tested
    @Transaction()
    public void deleteRuleForProduct(Context ctx, String product, String pdc) throws UnsupportedEncodingException{
        String acrKey = ctx.getClientIdentity().getMSPID() + ACR_STRING;
        
        if (privateObjectExists(ctx, acrKey, pdc)){ 
            byte[] acc = ctx.getStub().getPrivateData(pdc, acrKey);
            String accString = new String(acc, "UTF-8");
            AcceptRule acceptRule = AcceptRule.fromJSONString(accString); 
            acceptRule.deleteEntryFromProductToAttributeAndRule(product);
            ctx.getStub().putPrivateData(pdc, acrKey, acceptRule.toJSONString().getBytes(UTF_8));
        }
        else {
            throw new RuntimeException("There is no AcceptRule Object defined");
        }

    }

    @Transaction()
    public AcceptRule readAccept(Context ctx, String id, String pdc) throws UnsupportedEncodingException{
        byte[] acc = ctx.getStub().getPrivateData(pdc, id + ACR_STRING);
        String accString = new String(acc, "UTF-8");
        AcceptRule acr = AcceptRule.fromJSONString(accString);      
        return acr;
    }












    
    @Transaction()
    public String readAccept1(Context ctx) throws UnsupportedEncodingException{
        byte[] acc = ctx.getStub().getPrivateData("CollectionOne", ctx.getClientIdentity().getMSPID() + ACR_STRING);
        String accString = new String(acc, "UTF-8");
        AcceptRule acr = AcceptRule.fromJSONString(accString);
        //AcceptRule acNew = new AcceptRule();
        //acr.setProductToAttributeAndRule(acr.getProductToAttributeAndRule());
        acr.addEntryToProductToAttributeAndRule("milklot", "hallowelr", "jbjhbjb");
        ctx.getStub().putPrivateData("CollectionOne", ctx.getClientIdentity().getMSPID() + ACR_STRING, acr.toJSONString().getBytes(UTF_8));
        return acr.getProductToAttributeAndRule().toString();
    }
    
   
    
}

