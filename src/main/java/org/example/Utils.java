package org.example;

import static java.nio.charset.StandardCharsets.UTF_8;
import org.hyperledger.fabric.contract.Context;
import org.json.JSONObject;

public class Utils {

    String createReturnValue(String statusCode, Object message){
        JSONObject response = new JSONObject();
        response.put("status", statusCode);
        response.put("response", message);
        return response.toString();
    }

    boolean objectExists(Context ctx, String id){
        byte[] buffer = ctx.getStub().getState(id);
        return (buffer != null && buffer.length > 0);
    }

    public boolean privateObjectExists(Context ctx, String id, String pdc) {
        byte[] buffer = ctx.getStub().getPrivateDataHash(pdc, id);        
        return (buffer != null && buffer.length > 0);
    }

    public void putState(Context ctx, String id, MetaDef metaDef){
        ctx.getStub().putState(id, metaDef.toJSONString().getBytes(UTF_8)); 
    }

    public void putState(Context ctx, String id, MetaObject metaObject){
        ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8)); 
    }

    public void putPrivateData(Context ctx, String pdc, String id, PrivateMetaObject privateMetaObject){
        ctx.getStub().putPrivateData(pdc, id + NutriSafeContract.PDC_STRING, privateMetaObject.toJSONString().getBytes(UTF_8));
    }

    public void putPrivateData(Context ctx, String pdc, String id, MetaObject metaObject){
        ctx.getStub().putPrivateData(pdc, id, metaObject.toJSONString().getBytes(UTF_8));
    }

    public void putPrivateData(Context ctx, String pdc, String id, AcceptRule acceptRule){
        ctx.getStub().putPrivateData(pdc, id, acceptRule.toJSONString().getBytes(UTF_8));
    }

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

    public AcceptRule getAcceptRule(Context ctx, String pdc, String id){     
        try {
            byte[] acc = ctx.getStub().getPrivateData(pdc, id);
            String accString = new String(acc, "UTF-8");
            return AcceptRule.fromJSONString(accString);
        }
        catch (Exception e){
            return new AcceptRule();
        }
        
    }

    public MetaDef getMetaDef(Context ctx){
        return MetaDef.fromJSONString(new String(ctx.getStub().getState(NutriSafeContract.META_DEF_ID)));
    }

    public MetaObject getMetaObject(Context ctx, String id){
        return MetaObject.fromJSONString(new String(ctx.getStub().getState(id)));
    }
    
    
}