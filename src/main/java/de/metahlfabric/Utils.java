package de.metahlfabric;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ledger.KeyModification;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.util.ArrayList;

/**
 * Helper class for the {@link MFContract}.
 *
 * @author Tobias Wagner, Dennis Lamken
 * <p>
 * Copyright 2021 OTARIS Interactive Services GmbH
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
public class Utils {

    /**
     * Creates a return value with a status code and a message
     *
     * @param message the message to pass
     * @return the return message as json string
     */
    String createSuccessReturnValue(JsonElement message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "200");
        response.add("response", message);
        return response.toString();
    }

    String createReturnValue(String statusCode, String message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", statusCode);
        response.addProperty("response", message);
        return response.toString();
    }

    String createSuccessReturnValue(boolean message) {
        JsonObject response = new JsonObject();
        response.addProperty("status", "200");
        response.addProperty("response", message);
        return response.toString();
    }

    String createSuccessReturnValue(Object message) {
        return this.createSuccessReturnValue(new Gson().fromJson(new Gson().toJson(message), JsonObject.class));
    }

    /**
     * @param ctx the Hyperledger context object
     * @param id  the id to check
     * @return true, if object exists
     */
    boolean objectExists(Context ctx, String id) {
        byte[] buffer = ctx.getStub().getState(id);
        return (buffer != null && buffer.length > 0);
    }

    /**
     * @param ctx the Hyperledger context object
     * @param id  the id to check
     * @param pdc the private data collection where the object should be stored
     * @return true, if object exists
     */
    public boolean privateObjectExists(Context ctx, String id, String pdc) {
        byte[] buffer = ctx.getStub().getPrivateDataHash(pdc, id);
        return (buffer != null && buffer.length > 0);
    }

    /**
     * Writes the MetaDef to the ledger
     *
     * @param ctx     the Hyperledger context object
     * @param id      the corresponding id for the object
     * @param metaDef the MetaDef object to save
     */
    public void putState(Context ctx, String id, MetaDef metaDef) {
        ctx.getStub().putState(id, metaDef.toJSONString().getBytes(UTF_8));
    }

    /**
     * Writes the MetaObject to the ledger
     *
     * @param ctx        the Hyperledger context object
     * @param id         the corresponding id for the object
     * @param metaObject the MetaObject to save
     */
    public void putState(Context ctx, String id, MetaObject metaObject) {
        ctx.getStub().putState(id, metaObject.toJSONString().getBytes(UTF_8));
    }

    /**
     * Writes the PrivateMetaObject inside the specified private data collection
     *
     * @param ctx               the Hyperledger context object
     * @param pdc               the corresponding private data collection for the object
     * @param id                the corresponding id for the object
     * @param privateMetaObject the privateMetaObject to save
     */
    public void putPrivateData(Context ctx, String pdc, String id, PrivateMetaObject privateMetaObject) {
        ctx.getStub().putPrivateData(pdc, id, privateMetaObject.toJSONString().getBytes(UTF_8));
    }

    /**
     * Gets the PrivateMetaObject (if it does not exist, an empty Object will be created)
     *
     * @param ctx the Hyperledger context object
     * @param pdc the private data collection where the object should be stored
     * @param id  the corresponding id for the object
     * @return the privateMetaObject
     */
    public PrivateMetaObject getPrivateMetaObject(Context ctx, String pdc, String id) {
        try {
            byte[] pmoArray = ctx.getStub().getPrivateData(pdc, id);
            String pmoString = new String(pmoArray, UTF_8);
            return new Gson().fromJson(pmoString, PrivateMetaObject.class);
        } catch (Exception e) {
            return new PrivateMetaObject();
        }

    }

    /**
     * @param ctx the Hyperledger context object
     * @return the MetaDef object
     */
    public MetaDef getMetaDef(Context ctx) {
        if (!objectExists(ctx, MFContract.META_DEF_ID))
            return new MetaDef();
        else
            return new Gson().fromJson(new String(ctx.getStub().getState(MFContract.META_DEF_ID), UTF_8), MetaDef.class);
    }

    /**
     * @param ctx the Hyperledger context object
     * @param id  the corresponding id for the object
     * @return the MetaObject
     */
    public MetaObject getMetaObject(Context ctx, String id) {
        return new Gson().fromJson(new String(ctx.getStub().getState(id), UTF_8), MetaObject.class);
    }

     /**
     * @param ctx the Hyperledger context object
     * @param id  the corresponding id for the object
     * @return the MetaObject array of past states
     */
    public ArrayList<MetaObject> getObjectHistoryFromKey(Context ctx, String id) {
        ArrayList<MetaObject> objectHistory = new ArrayList<MetaObject>();
        QueryResultsIterator<KeyModification> objectChangesIterator = ctx.getStub().getHistoryForKey(id);
        while(objectChangesIterator.iterator().hasNext()) {
            KeyModification currentModification = objectChangesIterator.iterator().next();
            objectHistory.add(new Gson().fromJson(currentModification.getStringValue(), MetaObject.class));
        }
        return objectHistory;
    }



    /**
     * @param ctx     the Hyperledger context object
     * @param name    the event name
     * @param payload additional info about the event
     */
    public void emitEvent(Context ctx, String name, byte[] payload) {
        ctx.getStub().setEvent(name, payload);
    }

}