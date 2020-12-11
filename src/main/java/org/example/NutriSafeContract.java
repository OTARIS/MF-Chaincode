package org.example;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.json.JSONArray;
import org.hyperledger.fabric.contract.annotation.Contact;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.License;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * The main contract 
 */

@Contract(name = "NutriSafeContract",
    info = @Info(title = "NutriSafe contract",
                description = "Chaincode of the research project NutriSafe",
                version = "1",
                license =
                        @License(name = "Apache-2.0",
                                url = "https://www.nutrisafe.de"),
                                contact =  @Contact(name = "Tobias Wagner")))

@Default
public class NutriSafeContract implements ContractInterface {

    /**
     * The id where to find the MetaDef object
     */
    static String META_DEF_ID = "METADEF";

    /**
     * Suffix for the private part of a object
     */
    static String PDC_STRING = "_P";

    /**
     * Name of the state-owned private data collection
     */
    static String AUTHORITY_PDC = "AuthCollection";

    /**
     * The Utils object
     */
    Utils helper = new Utils();
    
    /**
     * Empty constructor
     */
    public  NutriSafeContract() {}

    /* #region utils */

    /**
     * Query the local state database (Couch DB query indexes)
     * 
     * @param ctx the hyperledger context object
     * @param queryString the query string 
     * @return a list of all objects that fulfill the condition
     */
    @Transaction
    public String queryChaincodeByQueryString(Context ctx, String queryString){
       
        QueryResultsIterator<KeyValue> result = ctx.getStub().getQueryResult(queryString);
        //"{\"selector\":{\"actualOwner\":\"Org1MSP\"}}"
       
        Iterator<KeyValue> it = result.iterator();
        JSONArray jsonArray = new JSONArray();
        while (it.hasNext()){
            jsonArray.put(it.next().getValue());
       
        }
        return helper.createReturnValue("200", jsonArray);        
    }

    /**
     * Checks if an object to the given id exists
     * 
     * @param ctx the hyperledger context object
     * @param id the id to check
     * 
     * @return true, if the object exists
     */
    @Transaction
    public String objectExists(Context ctx, String id){            
        
        byte[] buffer = ctx.getStub().getState(id);
        if (buffer != null && buffer.length > 0) return helper.createReturnValue("200", "true");
        else return helper.createReturnValue("200", "false");

    }

    /**
     * Checks if a private object to the given id exists
     * 
     * @param ctx the hyperledger context object
     * @param id the id to check
     * @param pdc the private data collection to check
     * 
     * @return true, if the private object exists (and we are allowed to see it)
     */
    @Transaction()
    public String privateObjectExists(Context ctx, String id, String pdc) {
        
        byte[] buffer = ctx.getStub().getPrivateDataHash(pdc, id);       
        if (buffer != null && buffer.length > 0) return helper.createReturnValue("200", "true");
        else return helper.createReturnValue("200", "false");
    }

    /**
     * Deletes the object
     * 
     * @param ctx the hyperledger context object
     * @param id the id to delete
     * 
     * @return 200, if object was deleted; 400, if the object does not exist
     */
    @Transaction()
    public String deleteObject(Context ctx, String id){
        
        if (!helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);
        for (String pdc : metaObject.getPrivateDataCollection()){
            deletePrivateObject(ctx, id + PDC_STRING, pdc);
        }
        
        ctx.getStub().delState(id);
        
        return helper.createReturnValue("200", "The object with the key " +id+ " was deleted");
    } 

    /**
     * Deletes the private object
     * 
     * @param ctx the hyperledger context object
     * @param id the id to delete
     * @param pdc the private data collection where to find the object
     * 
     * @return 200, if object was deleted; 400, if the object does not exist (or we are not allowed to delete it)
     */
    @Transaction()
    public String deletePrivateObject(Context ctx, String id, String pdc) {
        
        if (!helper.privateObjectExists(ctx, id, pdc)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist in the private data collection " +pdc);
        
        ctx.getStub().delPrivateData(pdc, id);
        
        return helper.createReturnValue("200", "The object with the key " +id+ " was deleted from the private data collection " +pdc);
    }

    /* #endregion */

    /* #region META definitions */

    /**
     * Reads the meta def
     * 
     * @param ctx the hyperledger context object
     * 
     * @return the meta def
     */
    @Transaction()
    public String META_readMetaDef(Context ctx){

        if (!helper.objectExists(ctx, META_DEF_ID))return helper.createReturnValue("400", "The meta def with the key " +META_DEF_ID+ " does not exist");

        MetaDef metaDef = helper.getMetaDef(ctx);

        return helper.createReturnValue("200", metaDef.toString());
    }

    @Transaction()
    public String META_readMetaDef(Context ctx, int version){

        if (!helper.objectExists(ctx, META_DEF_ID))return helper.createReturnValue("400", "The meta def with the key " +META_DEF_ID+ " does not exist");

        MetaDef metaDef = helper.getMetaDef(ctx);

        return helper.createReturnValue("200", metaDef.toString());
    }

    @Transaction()
    public String META_deleteProduct(Context ctx, String name){
        if (!helper.objectExists(ctx, META_DEF_ID))return helper.createReturnValue("400", "The meta def with the key " +META_DEF_ID+ " does not exist");

        MetaDef metaDef = helper.getMetaDef(ctx);
        if(!metaDef.productNameExists(name))return helper.createReturnValue("400", "The product with name " +name+ " doesn't exist");
        metaDef.deleteProductDefinition(name);
        helper.putState(ctx, META_DEF_ID, metaDef);
        return helper.createReturnValue("200", metaDef.toString());
    }
    /**
     * Adds an attribute to the meta def 
     * If no meta def exists, it will be created
     * 
     * @param ctx the hyperledger context object
     * @param attribute the name of the attribute to add
     * @param dataType the data type of the attribute to add
     * 
     * @return the meta def
     */
    @Transaction()
    public String META_addAttributeDefinition(Context ctx, String attribute, String dataType){
        MetaDef metaDef;
        if (!helper.objectExists(ctx, META_DEF_ID)){
            metaDef = new MetaDef();  
        }
        else {
            metaDef = helper.getMetaDef(ctx);
        }

        metaDef.addAttributeDefinition(attribute, dataType);
        helper.putState(ctx, META_DEF_ID, metaDef);
        
        return helper.createReturnValue("200", metaDef.toString());
       
    }

    /**
     * Adds a product to the meta def 
     * If no meta def exists, it will be created
     * 
     * @param ctx the hyperledger context object
     * @param productName the name of the new product 
     * @param attributes the attributes of the new product
     * 
     * @return the meta def
     */
    @Transaction()
    public String META_addProductDefinition(Context ctx, String productName, String[] attributes){

        MetaDef metaDef;
        if (!helper.objectExists(ctx, META_DEF_ID)){
            metaDef = new MetaDef();  
        }
        else {
            metaDef = helper.getMetaDef(ctx);
        }
                
        ArrayList<String> attributesArray = new ArrayList<>();
        HashMap<String, String> allowedAttributes = metaDef.getAttributeToDataTypeMap();
        
        for (int i = 0; i < attributes.length; i++){
            if (!allowedAttributes.containsKey(attributes[i])) return helper.createReturnValue("400", "The attribute " +attributes[i]+ " is not defined"); 
            attributesArray.add(attributes[i]);                       
        
        }
        metaDef.addProductDefinition(productName, attributesArray);
        helper.putState(ctx, META_DEF_ID, metaDef); 
        
        return helper.createReturnValue("200", metaDef.toString());  
    }

    /**
     * Adds an unit to the meta def
     * If no meta def exists, it will be created
     * 
     * @param ctx the hyperledger context object
     * @param unit the name of the unit
     * 
     * @return the meta def
     */
    @Transaction()
    public String META_addUnit(Context ctx, String unit){

        MetaDef metaDef;
        if (!helper.objectExists(ctx, META_DEF_ID)) metaDef = new MetaDef();  
        else metaDef = helper.getMetaDef(ctx);

        if (metaDef.getUnitList().contains(unit)) return helper.createReturnValue("400", "The unit " +unit+ " is still defined"); 
        
        metaDef.addUnitToUnitList(unit);
        helper.putState(ctx, META_DEF_ID, metaDef); 

        return helper.createReturnValue("200", metaDef.toString());  
    }


    /* #endregion */

    /* #region META objects */

    /**
     * Creates a new object (Pass transient data, to create private attribute ("attribute":"value"))
     * 
     * @param ctx the hyperledger context object
     * @param id the id of the object
     * @param pdc the private data collection where to store the private data (empty if no private data necessary)
     * @param productName the product name of this objects (defined in the MetaDef)
     * @param amount the initial amount of this object
     * @param unit the unit definition of this object
     * @param attributes the names of all attributes (defined in the MetaDef)
     * @param attrValues the values of this object corresponding the attribute names
     *
     * @throws UnsupportedEncodingException if private object can't be decoded
     * 
     * @return the object
     */
    @Transaction()
    public String createObject(Context ctx, String id, String pdc, String productName, String amount, String unit, String[] attributes, String[] attrValues)throws UnsupportedEncodingException{
        
        if (helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " already exists");
        
        MetaDef metaDef = helper.getMetaDef(ctx);
        if (!metaDef.productNameExists(productName)) return helper.createReturnValue("400", "The product name " +productName+ " is not defined");

        if (!metaDef.getUnitList().contains(unit)) return helper.createReturnValue("400", "The unit " +unit+ " is not defined");

        Double amountDouble = 0.0;
        try {
            amountDouble = Double.parseDouble(amount);
        }
        catch (Exception e) {
            return helper.createReturnValue("400", "The amount " +amount+ " is not a double");
        }
        
        List<String> allowedAttr = metaDef.getAttributesByProductName(productName);       
        int i = 0;
        for(String attr : attributes){
            
            if (!allowedAttr.contains(attr)) return helper.createReturnValue("400", "The attribute " +attr+ " is not defined");
            
            if (metaDef.getDataTypeByAttribute(attr).equals("Integer") && !attrValues[i].matches("-?\\d+")) return helper.createReturnValue("400", "The attribute " +attr+ " is not an Integer");
        i++;
        }

        i = 0;
        Map<String, byte[]> transientData = ctx.getStub().getTransient();
        if (transientData.size() != 0) {
            
            if (pdc.equals("")) return helper.createReturnValue("400", "Please select a private data collection to store the private data");
            
            for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                
                if (!allowedAttr.contains(entry.getKey())) return helper.createReturnValue("400", "The attribute " +entry.getKey()+ " is not defined");
                
                if (metaDef.getDataTypeByAttribute(entry.getKey()).equals("Integer")){
                    
                    String value = new String(entry.getValue(), "UTF-8");
                    
                    if (!value.matches("-?\\d+")) return helper.createReturnValue("400", "The attribute " +entry.getKey()+ " is not an Integer");
                }
                i++;
            
            }
            
            PrivateMetaObject privateMetaObject = new PrivateMetaObject();
            for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                privateMetaObject.addAttribute(entry.getKey(), new String(entry.getValue(), "UTF-8"));
            }
            
            helper.putPrivateData(ctx, pdc, id + PDC_STRING, privateMetaObject);
        }

        String timeStamp = ctx.getStub().getTxTimestamp().toString();
        MetaObject metaObject = new MetaObject(pdc, productName, amountDouble, unit, attributes, attrValues, timeStamp, ctx.getClientIdentity().getMSPID());
        metaObject.setKey(id);
        helper.putState(ctx, id, metaObject);

        return helper.createReturnValue("200", metaObject.toString());      
    }

    /**
     * Reads an object
     * 
     * @param ctx the hyperledger context object
     * @param id the id of the object
     * 
     * @return the object
     */
    @Transaction()
    public String readObject(Context ctx, String id){
        
        if (!helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");  

        MetaObject metaObject = helper.getMetaObject(ctx, id);

        HashMap<String, String> pdcMap = new HashMap<>();

        for (String pdc : metaObject.getPrivateDataCollection()){
            PrivateMetaObject privateMetaObject = helper.getPrivateMetaObject(ctx, pdc, id + PDC_STRING);                
            pdcMap.putAll(privateMetaObject.getAttributes());
        }
        return helper.createReturnValue("200", metaObject.toString() + "Private Data: " + pdcMap); 
    }

    /**
     * Sets the receiver
     * 
     * @param ctx the hyperledger context object
     * @param id the id of the object
     * @param receiver the receiver to set
     * 
     * @return the object
     */
    @Transaction()
    public String setReceiver(Context ctx, String id, String receiver){
        
        if (!helper.objectExists(ctx, id))return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");
        
        MetaObject metaObject = helper.getMetaObject(ctx, id);
         
        if (!metaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID())) return helper.createReturnValue("400", "You (" + ctx.getClientIdentity().getMSPID() + ") are not the actual owner");
        
        metaObject.setReceiver(receiver); 
        helper.putState(ctx, id, metaObject);

        return helper.createReturnValue("200", metaObject.toString());
    }

    /**
     * Changes the owner
     * 
     * @param ctx the hyperledger context object
     * @param id the id of the object
     * 
     * @return the object
     */
    @Transaction()
    public String changeOwner(Context ctx, String id){
        
        if (!helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);
        String newOwner = ctx.getClientIdentity().getMSPID();

        if (!metaObject.getReceiver().equals(newOwner)) return helper.createReturnValue("400", "You (" + ctx.getClientIdentity().getMSPID() + ") are not the receiver");

        metaObject.setReceiver("");
        metaObject.addTsAndOwner(ctx.getStub().getTxTimestamp().toString(), newOwner);
        metaObject.setActualOwner(newOwner);
        helper.putState(ctx, id, metaObject);

        return helper.createReturnValue("200", metaObject.toString());
    }

    /**
     * Adds a predecessor
     * 
     * @param ctx the hyperledger context object
     * @param predecessorId the id of the predecessor
     * @param id the id of the successor
     * @param amountDif the amount that was transferred
     * 
     * @return the successor object
     */
    @Transaction()
    public String addPredecessor(Context ctx, String predecessorId, String id, String amountDif){

        if (!helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");

        if (!helper.objectExists(ctx, predecessorId)) return helper.createReturnValue("400", "The object with the key " +predecessorId+ " does not exist");
       
        MetaObject metaObject = helper.getMetaObject(ctx, id);
        if (!metaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID())) return helper.createReturnValue("400", "You are not the owner of " +id);
    
        MetaObject preMetaObject = helper.getMetaObject(ctx, predecessorId);
        if (!preMetaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID())) return helper.createReturnValue("400", "You are not the owner of " +predecessorId);
        
        preMetaObject.addSuccessor(id, amountDif.substring(1)  + " " + preMetaObject.getUnit());
        preMetaObject.addAmount(Double.parseDouble(amountDif));

        if (preMetaObject.getAmount() < 0.0) return helper.createReturnValue("400", "The amount is lower than zero");

        helper.putState(ctx, predecessorId, preMetaObject);
        
        metaObject.addPredecessor(predecessorId, amountDif.substring(1) + " " + preMetaObject.getUnit() + " " + preMetaObject.getProductName());
        helper.putState(ctx, id, metaObject);

        return helper.createReturnValue("200", metaObject.toString());         
    }
    
    /**
     * Updates an object
     * 
     * @param ctx the hyperledger context object
     * @param id the id of the object
     * @param attrName the name of the attribute
     * @param attrValue the new value of this attribute
     * 
     * @return the object
     */
    @Transaction()
    public String updateAttribute(Context ctx, String id, String attrName, String attrValue){
        
        if (!helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");
       
        MetaObject metaObject = helper.getMetaObject(ctx, id);

        MetaDef metaDef = helper.getMetaDef(ctx);

        List<String> allowedAttr = metaDef.getAttributesByProductName(metaObject.getProductName());


        if (!allowedAttr.contains(attrName)) return helper.createReturnValue("400", "The attrName "+attrName+  " is not defined");

        if (metaDef.getDataTypeByAttribute(attrName).equals("Integer")){

            if (!attrValue.matches("-?\\d+")) return helper.createReturnValue("400", "The attribute " +attrName+ " is not an Integer");
        }

        metaObject.addAttribute(attrName, attrValue);
        helper.putState(ctx, id, metaObject);

        return helper.createReturnValue("200", metaObject.toString()); 
    
        
    } 

    /**
     * Updates a private attribute (Transient data("attribute":"value"))
     * 
     * @param ctx the hyperledger context object
     * @param id the id of the object
     * @param pdc the private data collection to store the private data
     *
     * @throws UnsupportedEncodingException if private object can't be decoded
     * 
     * @return the object
     */
    @Transaction()
    public String updatePrivateAttribute(Context ctx, String id, String pdc) throws UnsupportedEncodingException{

        if (!helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);

        MetaDef metaDef = helper.getMetaDef(ctx);
        List<String> allowedAttr = metaDef.getAttributesByProductName(metaObject.getProductName());

        Map<String, byte[]> transientData = ctx.getStub().getTransient();
        if (transientData.size() == 0) return helper.createReturnValue("400", "No transient data passed");  

        for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
                
            if (!allowedAttr.contains(entry.getKey())) return helper.createReturnValue("400", "The attrName "+entry.getKey()+  " is not defined");
            
            if (metaDef.getDataTypeByAttribute(entry.getKey()).equals("Integer")){
                
                String value = new String(entry.getValue(), "UTF-8");                  
                if (!value.matches("-?\\d+")) return helper.createReturnValue("400", "The attribute " +entry.getKey()+ " is not an Integer");        
            }   
        }

        PrivateMetaObject privateMetaObject = new PrivateMetaObject();

        if (helper.privateObjectExists(ctx, id + PDC_STRING, pdc)){
                
            privateMetaObject = helper.getPrivateMetaObject(ctx, pdc, id + PDC_STRING);
        }
        if (!metaObject.getPrivateDataCollection().contains(pdc)){
            metaObject.addPrivateDataCollection(pdc);
        }

        for (Map.Entry<String, byte[]> entry : transientData.entrySet()){
            privateMetaObject.addAttribute(entry.getKey(), new String(entry.getValue(), "UTF-8"));
        }

        helper.putPrivateData(ctx, pdc, id + PDC_STRING, privateMetaObject);
        return helper.createReturnValue("200", privateMetaObject.toString());
    }
    
    /* #endregion */
    
    /* #region alarm handling */

     /**
     * Activates the alarm (All successors will be informed)
     * 
     * @param ctx the hyperledger context object
     * @param id the id of the object
     * 
     * @return the object
     */
    @Transaction()
    public String activateAlarm(Context ctx, String id){
        
        //TODO Prüfung auf Berechtigung

        if (!helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);
        metaObject.setAlarmFlag(true);
        helper.putState(ctx, id, metaObject);

        HashMap<String, String> successors = metaObject.getSuccessor();

        for (String suc : successors.keySet()){
            MetaObject sucMetaObject = helper.getMetaObject(ctx, suc);
            sucMetaObject.setAlarmFlag(true);
            helper.putState(ctx, suc, metaObject);
        }
        helper.emitEvent(ctx, "alarm_activated", metaObject.toString().getBytes());

        return helper.createReturnValue("200", metaObject.toString());
    }

    /**
     * Dectivates the alarm (All successors will be informed)
     *
     * @param ctx
     * @param id
     *
     * @return the object
     */
    @Transaction()
    public String deactivateAlarm(Context ctx, String id){

        //TODO Prüfung auf Berechtigung

        if (!helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);
        metaObject.setAlarmFlag(false);
        helper.putState(ctx, id, metaObject);

        HashMap<String, String> successors = metaObject.getSuccessor();

        for (String suc : successors.keySet()){
            MetaObject sucMetaObject = helper.getMetaObject(ctx, suc);
            sucMetaObject.setAlarmFlag(false);
            helper.putState(ctx, suc, metaObject);
        }

        return helper.createReturnValue("200", metaObject.toString());
    }
    /**
     * Exports the information of an alarm object to the auth collection
     * 
     * @param ctx the hyperledger context object
     * @param id the id of the object
     * 
     * @return the object
     */
    /*
    //not tested
    @Transaction()
    public String exportDataToAuthPDC(Context ctx, String id) throws UnsupportedEncodingException{

        if (!helper.objectExists(ctx, id)) return helper.createReturnValue("400", "The object with the key " +id+ " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);
       
        if (!metaObject.getAlarmFlag() == true) return helper.createReturnValue("400", "The alarm flag for " +id+  "is set to false");
        
        
        if (metaObject.getPrivateDataCollection().length() > 2){
            PrivateMetaObject privateMetaObject = helper.getPrivateMetaObject(ctx, metaObject.getPrivateDataCollection(), id + PDC_STRING);
            metaObject.addAllAttributes(privateMetaObject.getAttributes());
        }

        metaObject.setPrivateDataCollection("");
        helper.putPrivateData(ctx, AUTHORITY_PDC, id, metaObject);

        return helper.createReturnValue("200", metaObject.toString());       
    }
    */

    /* #endregion */
}

