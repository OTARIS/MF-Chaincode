package org.example;

import static java.nio.charset.StandardCharsets.UTF_8;
import org.hyperledger.fabric.contract.Context;
import org.json.JSONObject;

/**
 * Helper class for the NutriSafeContract
 */

public class Utils {

    /**
     * Creates a return value with a status code and a message
     * 
     * @param statusCode the status code to pass (200 ok, 400 error)
     * @param message the message to pass
     * 
     * @return the return message as json string
     */
    String createReturnValue(String statusCode, Object message){
        JSONObject response = new JSONObject();
        response.put("status", statusCode);
        response.put("response", message);
        return response.toString();
    }

    /**
     * @param ctx the hyperledger context object
     * @param id the id to check
     * 
     * @return true, if object exists
     */
    boolean objectExists(Context ctx, String id){
        byte[] buffer = ctx.getStub().getState(id);
        return (buffer != null && buffer.length > 0);
    }

    /**
     * @param ctx the hyperledger context object
     * @param id the id to check
     * @param pdc the private data collection where the object should be stored
     * 
     * @return true, if object exists
     */
    public boolean privateObjectExists(Context ctx, String id, String pdc) {
        byte[] buffer = ctx.getStub().getPrivateDataHash(pdc, id);        
        return (buffer != null && buffer.length > 0);
    }

    /**
     * Writes the MetaDef to the ledger
     * 
     * @param ctx the hyperledger context object
     * @param id the corresponding id for the object
     * @param metaDef the MetaDef object to save
     */
    public void putState(Context ctx, String id, MetaDef metaDef){
        ctx.getStub().putState(id, metaDef.toJSONString().getBytes(UTF_8)); 
    }

    /**
     * Writes the MetaObject to the ledger
     * 
     * @param ctx the hyperledger context object
     * @param id the corresponding id for the object
     * @param metaObject the MetaObject to save
     */
    public void putState(Context ctx, String id, MetaObject metaObject){
        ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8)); 
    }

    /**
     * Writes the PrivateMetaObject inside the specified private data collection
     * 
     * @param ctx the hyperledger context object
     * @param pdc the corresponding private data collection for the object
     * @param id the corresponding id for the object
     * @param privateMetaObject the privateMetaObject to save
     */
    public void putPrivateData(Context ctx, String pdc, String id, PrivateMetaObject privateMetaObject){
        ctx.getStub().putPrivateData(pdc, id, privateMetaObject.toJSONString().getBytes(UTF_8));
    }

    /**
     * Gets the PrivateMetaObject (if it does not exist, an empty Object will be created)
     * 
     * @param ctx the hyperledger context object
     * @param pdc the private data collection where the object should be stored
     * @param id the corresponding id for the object
     *
     * @return the privateMetaObject
     */
    public PrivateMetaObject getPrivateMetaObject(Context ctx, String pdc, String id){     
        try {
            byte[] pmoArray = ctx.getStub().getPrivateData(pdc, id);
            String pmoString = new String(pmoArray, "UTF-8"); 
            return PrivateMetaObject.fromJSONString(pmoString); 
        }
        catch (Exception e){
            return new PrivateMetaObject();
        }
        
    }

    /**
     * @param ctx the hyperledger context object
     * 
     * @return the MetaDef object
     */
    public MetaDef getMetaDef(Context ctx){
        return MetaDef.fromJSONString(new String(ctx.getStub().getState(NutriSafeContract.META_DEF_ID)));
    }

    /**
     * @param ctx the hyperledger context object
     * @param id the corresponding id for the object
     * 
     * @return the MetaObject
     */
    public MetaObject getMetaObject(Context ctx, String id){
        return MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
    }

    /**
     * @param ctx
     * @param name
     * @param payload
     */
    public void emitEvent(Context ctx, String name, byte[] payload){
        ctx.getStub().setEvent(name, payload);
    }
    
}