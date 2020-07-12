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

    public  NutriSafeContract() {
    }

    //## META ######################################################################################
    @Transaction()
    public void META_createSampleData(Context ctx){
        MetaDef metaDef = new MetaDef();
        metaDef.createSampleData();
        ctx.getStub().putState(META_DEF_ID, metaDef.toJSONString().getBytes(UTF_8));     
    }

    @Transaction()
    public MetaDef META_readMetaDef(Context ctx){
        MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));
        return metaDef;
    }

    @Transaction()
    public void META_addFieldType(Context ctx, String dataField, String dataType){
        MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));
        if (metaDef.fieldExists(dataField)){
            throw new RuntimeException("The dataField "+dataField+" already exists");
        }
        else {
            metaDef.addFieldType(dataField, dataType);
            ctx.getStub().putState(META_DEF_ID, metaDef.toJSONString().getBytes(UTF_8));
        }
    }

    @Transaction()
    public void META_addDataName(Context ctx, String dataName, String[] fieldNames){
        MetaDef metaDef = MetaDef.fromJSONString(new String(ctx.getStub().getState(META_DEF_ID)));           
        ArrayList<String> fieldNameArray = new ArrayList<>();
        HashMap<String, String> allowedFields = metaDef.getFieldToTypeMap();
        for (int i = 0; i < fieldNames.length; i++){
            if (allowedFields.containsKey(fieldNames[i])){
                fieldNameArray.add(fieldNames[i]);
            }
            else {
                throw new RuntimeException("The field " +fieldNames[i] + " is not defined");
                
            }
        }
        metaDef.addDataName(dataName, fieldNameArray);
        ctx.getStub().putState(META_DEF_ID, metaDef.toJSONString().getBytes(UTF_8));      
    }

    @Transaction()
    public boolean objectExists(Context ctx, String id){
        byte[] buffer = ctx.getStub().getState(id);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void META_delete(Context ctx){
        boolean exists = objectExists(ctx, META_DEF_ID);
        if (!exists) {
            throw new RuntimeException("The metaDef does not exist");
        }
        ctx.getStub().delState(META_DEF_ID);
    } 

    //########################################################################################

    @Transaction()
    public void createObject(Context ctx, String id, String pdcName, String dataName, String[] attrNames, String[] attrValues, String timeStamp)throws UnsupportedEncodingException{
        //TODO Prüfung auf richtigen Datentyp
        if (!objectExists(ctx, id)){
            MetaDef metaDef = META_readMetaDef(ctx);           
            if (metaDef.dataNameExists(dataName)){
                List<String> allowedAttr = metaDef.getFieldsByDataName(dataName);
                boolean allAttrAllowed = true;
                for(String attr : attrNames){
                    if (!allowedAttr.contains(attr)) allAttrAllowed = false;
                }
                Map<String, byte[]> transientData = ctx.getStub().getTransient();
                if (transientData.size() != 0) {
                    for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                        if (!allowedAttr.contains(entry.getKey())) allAttrAllowed = false;
                    }
                }        
                if (allAttrAllowed){
                    MetaObject metaObject = new MetaObject(dataName, attrNames, attrValues, timeStamp, ctx.getClientIdentity().getMSPID());
                    ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
                    PrivateMetaObject privateMetaObject = new PrivateMetaObject();
                    for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                        privateMetaObject.addAttribute(entry.getKey(), new String(entry.getValue(), "UTF-8"));
                    }
                    ctx.getStub().putPrivateData(pdcName, id + PDC_STRING, privateMetaObject.toJSONString().getBytes(UTF_8));
                }
                else {
                    throw new RuntimeException("At least one illegal attribute name recognized");
                }
            }
            else{
                throw new RuntimeException("The dataName" +dataName+ "does not exist");
            }
        }
        else {
            throw new RuntimeException("The ID "+id+" already exists");
        }       
    }

    @Transaction()
    public String readObject(Context ctx, String id){
        //TODO Name der PDC in MetaObject speichern
        if (objectExists(ctx, id)){
            String pmoString = "";
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
 
            try {
                byte[] privateMetaObject = ctx.getStub().getPrivateData("CollectionOne", id + PDC_STRING);
                pmoString = new String(privateMetaObject, "UTF-8");
                //PrivateMetaObject pmo = PrivateMetaObject.fromJSONString(pmoString);
 
                
            }
            catch (Exception e){}
            String result = metaObject.toString() + "Private Data: \n" + pmoString;
            return result;
        }      
        else {
            throw new RuntimeException("The ID "+id+" does not exist");
        }
    }

    @Transaction()
    public void setReceiver(Context ctx, String id, String receiver){
        if (objectExists(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (metaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID())){
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
    }

    @Transaction()
    public void changeOwner(Context ctx, String id, String timeStamp){
        if (objectExists(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (metaObject.getReceiver().equals(ctx.getClientIdentity().getMSPID())){
                metaObject.setReceiver("");
                metaObject.addTsAndOwner(timeStamp, ctx.getClientIdentity().getMSPID());
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
    public void addPredecessor(Context ctx, String predecessorId, String id){
        //TODO Übergabe von Liste an Predecessor
        //TODO Prüfen ob man Eigentümer ist
        if (objectExists(ctx, id) && objectExists(ctx, predecessorId)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            metaObject.addPredecessor(predecessorId);
            ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
            metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(predecessorId)));
            metaObject.addSuccessor(id);
            ctx.getStub().putState(predecessorId, metaObject.toJSONString().getBytes(UTF_8));
        }
        else {
            throw new RuntimeException("The ID "+id+" or "+predecessorId+  " does not exist");
        }
    }

    @Transaction()
    public void updateAttribute(Context ctx, String id, String attrName, String attrValue){
        //TODO update private data
        if (objectExists(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            MetaDef metaDef = META_readMetaDef(ctx);
            List<String> allowedAttr = metaDef.getFieldsByDataName(metaObject.getDataName());
            if (attrName != ""){
                if (allowedAttr.contains(attrName) && attrName != ""){
                    metaObject.addAttribute(attrName, attrValue);
                    ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
                }
                else {
                    throw new RuntimeException("The attrName "+attrName+  " is not defined");
                }  
            }
            //TODO Derzeit müssen alle privaten Attribute nochmal erneut mitgegeben werden. Sollte geändert werden
            boolean allAttrAllowed = true;
            Map<String, byte[]> transientData = ctx.getStub().getTransient();
            if (transientData.size() != 0) {
                for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                    if (!allowedAttr.contains(entry.getKey())) allAttrAllowed = false;
                }
            }
            else {
                allAttrAllowed = false;
            } 

            if (allAttrAllowed){
                try {
                //byte[] privateMetaObject = ctx.getStub().getPrivateData("CollectionOne", id + PDC_STRING);
                //String pmoString = new String(privateMetaObject, "UTF-8");
                //PrivateMetaObject pmo = PrivateMetaObject.fromJSONString(pmoString);

                PrivateMetaObject privateMetaObject = new PrivateMetaObject();
                for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                    privateMetaObject.addAttribute(entry.getKey(), new String(entry.getValue(), "UTF-8"));
                }
                ctx.getStub().putPrivateData("CollectionOne", id + PDC_STRING, privateMetaObject.toJSONString().getBytes(UTF_8));
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


// AcceptRule###################################################################

    @Transaction()
    public void addRuleNameAndCondition(Context ctx, String ruleName, String[] attributes, String[] rules){
        //if (!objectExists(ctx, ctx.getClientIdentity().getMSPID() + ACR_STRING)){
        //  AcceptRule acceptRule = new AcceptRule();

        //}
    }

    @Transaction()
    public void addNewAccept(Context ctx,String id, String test){
        AcceptRule acceptRule = new AcceptRule();
        acceptRule.setTest(test);
        ctx.getStub().putPrivateData("CollectionOne", id, acceptRule.toJSONString().getBytes(UTF_8));

    }

    @Transaction()
    public String readAccept(Context ctx, String id) throws UnsupportedEncodingException{
        byte[] acc = ctx.getStub().getPrivateData("CollectionOne", id);
        String accString = new String(acc, "UTF-8");
        AcceptRule acr = AcceptRule.fromJSONString(accString);
        return acr.toString1();
    }
    
}

