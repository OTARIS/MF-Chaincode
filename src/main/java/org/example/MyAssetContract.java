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

@Contract(name = "MyAssetContract",
    info = @Info(title = "MyAsset contract",
                description = "My Smart Contract",
                version = "0.0.1",
                license =
                        @License(name = "Apache-2.0",
                                url = ""),
                                contact =  @Contact(email = "ChaincodeMeta@example.com",
                                                name = "ChaincodeMeta",
                                                url = "http://ChaincodeMeta.me")))
@Default
public class MyAssetContract implements ContractInterface {

    private String META_DEF_ID = "METADEF";

    public  MyAssetContract() {

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
    public boolean ObjectExists(Context ctx, String id){
        byte[] buffer = ctx.getStub().getState(id);
        return (buffer != null && buffer.length > 0);
    }

    @Transaction()
    public void META_delete(Context ctx){
        boolean exists = ObjectExists(ctx, META_DEF_ID);
        if (!exists) {
            throw new RuntimeException("The metaDef does not exist");
        }
        ctx.getStub().delState(META_DEF_ID);
    } 

    //########################################################################################

    @Transaction()
    public void createObject(Context ctx, String id, String dataName, String[] attrNames, String[] attrValues, String timeStamp){
        //TODO Prüfung auf richtigen Datentyp
        if (!ObjectExists(ctx, id)){
            MetaDef metaDef = META_readMetaDef(ctx);
            
            if (metaDef.dataNameExists(dataName)){
                List<String> allowedAttr = metaDef.getFieldsByDataName(dataName);

                boolean allAttrAllowed = true;
                for(String attr : attrNames){
                    if (!allowedAttr.contains(attr)) allAttrAllowed = false;
                }
                if (allAttrAllowed){
                    MetaObject metaObject = new MetaObject(dataName, attrNames, attrValues, timeStamp, ctx.getClientIdentity().getMSPID());
                    ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));

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
        if (ObjectExists(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            return metaObject.toString();
        }
        else {
            throw new RuntimeException("The ID "+id+" does not exist");
        }

    }

    @Transaction()
    public void setReceiver(Context ctx, String id, String receiver){
        if (ObjectExists(ctx, id)){
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
        if (ObjectExists(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            if (metaObject.getReceiver().equals(ctx.getClientIdentity().getMSPID())){
                metaObject.setReceiver("");
                metaObject.addTsAndOwner(timeStamp, ctx.getClientIdentity().getMSPID());
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
        if (ObjectExists(ctx, id) && ObjectExists(ctx, predecessorId)){
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
        if (ObjectExists(ctx, id)){
            MetaObject metaObject = MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
            MetaDef metaDef = META_readMetaDef(ctx);
            List<String> allowedAttr = metaDef.getFieldsByDataName(metaObject.getDataName());
            if (allowedAttr.contains(attrName)){
                metaObject.addAttribute(attrName, attrValue);
                ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
            }
            else {
                throw new RuntimeException("The attrName "+attrName+  " is not defined");
            }  
        }
        else {
            throw new RuntimeException("The ID "+id+" does not exist");
        }
    }

    //Private Data Collection###############################################//#endregion

    @Transaction()
    public void createPrivateData(Context ctx,String id) throws UnsupportedEncodingException{
        PrivateMetaObject pmo = new PrivateMetaObject();
        Map<String, byte[]> transientData = ctx.getStub().getTransient();
        
        pmo.setTest(new String(transientData.get("value"), "UTF-8"));
        ctx.getStub().putPrivateData("CollectionOne", id, pmo.toJSONString().getBytes(UTF_8));
    }

    @Transaction()
    public String readPrivateData(Context ctx, String id) throws UnsupportedEncodingException{
        byte[] pmo = ctx.getStub().getPrivateData("CollectionOne", id);
        String pmoString = new String(pmo, "UTF-8");
        return pmoString;

    }
}
