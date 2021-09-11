package de.metahlfabric;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

/**
 * The main contract of the MetaHL Fabric Framework. Here are all functions gathered that can be accessed by network
 * members.
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
@Contract(name = "MFContract",
        info = @Info(title = "MetaHL Fabric contract",
                description = "Chain code of the MetaHL Fabric Framework",
                version = "1",
                license =
                @License(name = "Apache-2.0",
                        url = "https://github.com/OTARIS/"),
                contact = @Contact(name = "Dennis Lamken")))

@Default
public class MFContract implements ContractInterface {

    /**
     * The id where to find the MetaDef object
     */
    static String META_DEF_ID = "METADEF";

    /**
     * Suffix for the private part of a object
     */
    static String PDC_STRING = "_P";

    /**
     * The Utils object
     */
    Utils helper = new Utils();

    /**
     * Empty constructor
     */
    public MFContract() {
    }

    /* #region META definitions */

    /**
     * Reads the meta def
     *
     * @param ctx     the Hyperledger context object
     * @param asset the name of the product
     * @param version the version of the meta definition
     * @return the meta def
     */
    @Transaction()
    public String getAttributeDefinitionsOfAsset(Context ctx, String asset, String version) {
        if (!helper.objectExists(ctx, META_DEF_ID))
            return helper.createReturnValue("400", "The meta def with the key " + META_DEF_ID
                    + " does not exist");
        int versionNumber;
        try {
            versionNumber = Integer.parseInt(version);
        } catch (NumberFormatException e) {
            return helper.createReturnValue("400", "Malformatted version number");
        }

        MetaDef metaDef = helper.getMetaDef(ctx);

        List<MetaDef.AttributeDefinition> attributeDefinitions = metaDef.getAttributesByAssetNameAndVersion(asset,
                versionNumber);

        if (attributeDefinitions == null) {
            if (!metaDef.assetNameExists(asset))
                return helper.createReturnValue("400", "Product does not exist");
            else
                return helper.createReturnValue("400", "Version does not exist");
        }
        JsonObject result = new JsonObject();
        result.add("attributes",
                new Gson().fromJson(new Gson().toJson(attributeDefinitions.toArray(new MetaDef.AttributeDefinition[0])),
                        JsonArray.class));
        return helper.createSuccessReturnValue(result);
    }

    /**
     * Reads the meta def
     *
     * @param ctx       the Hyperledger context object
     * @param attribute the name of the attribute
     * @param version   the version of the meta definition
     * @return the meta def
     */
    @Transaction()
    public String getDataTypeOfAttributeDefinition(Context ctx, String attribute, String version) {
        if (!helper.objectExists(ctx, META_DEF_ID))
            return helper.createReturnValue("400", "The meta def with the key " + META_DEF_ID
                    + " does not exist");
        int versionNumber;
        try {
            versionNumber = Integer.parseInt(version);
        } catch (NumberFormatException e) {
            return helper.createReturnValue("400", "Malformatted version number");
        }

        MetaDef metaDef = helper.getMetaDef(ctx);

        List<MetaDef.AttributeDefinition> attributeDefinitions = metaDef.getAttributeList();

        if (attributeDefinitions != null)
            for (MetaDef.AttributeDefinition metaDefinition : attributeDefinitions)
                if (metaDefinition.getName().equalsIgnoreCase(attribute)) {
                    MetaDef.AttributeDataType dataType = metaDefinition.getDataType(versionNumber);
                    if (dataType == null)
                        return helper.createReturnValue("400", "Version does not exist");
                    else {
                        return helper.createReturnValue("200", dataType.toString());
                    }
                }

        return helper.createReturnValue("400", "Attribute does not exist");
    }

    /**
     * Reads the meta def
     *
     * @param ctx the Hyperledger context object
     * @return the meta def
     */
    @Transaction()
    public String getAssetDefinition(Context ctx, String product) {

        if (!helper.objectExists(ctx, META_DEF_ID))
            return helper.createReturnValue("400", "The meta def with the key " + META_DEF_ID
                    + " does not exist");

        MetaDef metaDef = helper.getMetaDef(ctx);

        List<MetaDef.AssetDefinition> assetDefinitions = metaDef.getAssetDefinitions();
        for (MetaDef.AssetDefinition assetDefinition : assetDefinitions) {
            if (assetDefinition.getName().equalsIgnoreCase(product))
                return helper.createSuccessReturnValue(assetDefinition);
        }
        return helper.createReturnValue("400", "Product does not exist");

    }

    /**
     * Reads the meta def
     *
     * @param ctx the Hyperledger context object
     * @return the meta def
     */
    @Transaction()
    public String getDefinition(Context ctx) {

        if (!helper.objectExists(ctx, META_DEF_ID))
            return helper.createReturnValue("400", "The meta def with the key " + META_DEF_ID + " does not exist");

        MetaDef metaDef = helper.getMetaDef(ctx);

        return helper.createSuccessReturnValue(metaDef.toJSON());
    }

    @Transaction()
    public String removeAssetDefinition(Context ctx, String name) {
        if (!helper.objectExists(ctx, META_DEF_ID))
            return helper.createReturnValue("400", "The meta def with the key " + META_DEF_ID + " does not exist");

        MetaDef metaDef = helper.getMetaDef(ctx);
        if (!metaDef.deleteAssetDefinition(name))
            return helper.createReturnValue("400", "The product with name " + name + " doesn't exist");
        helper.putState(ctx, META_DEF_ID, metaDef);
        return helper.createSuccessReturnValue(metaDef.toJSON());
    }

    /**
     * Adds an attribute to the meta def
     * If no meta def exists, it will be created
     *
     * @param ctx       the Hyperledger context object
     * @param attribute the name of the attribute to add
     * @param dataType  the data type of the attribute to add
     * @return the meta def
     */
    @Transaction()
    public String putAttributeDefinition(Context ctx, String attribute, String dataType) {
        MetaDef metaDef;
        if (!helper.objectExists(ctx, META_DEF_ID)) {
            metaDef = new MetaDef();
        } else {
            metaDef = helper.getMetaDef(ctx);
        }

        metaDef.addAttributeDefinition(attribute, dataType);
        helper.putState(ctx, META_DEF_ID, metaDef);

        return helper.createSuccessReturnValue(metaDef.toJSON());

    }

    /**
     * Adds a product to the meta def
     * If no meta def exists, it will be created
     *
     * @param ctx         the Hyperledger context object
     * @param name the name of the new product
     * @param attributes  the attributes of the new product
     * @return the meta def
     */
    @Transaction()
    public String putAssetDefinition(Context ctx, String name, String[] attributes) {

        MetaDef metaDef;
        if (!helper.objectExists(ctx, META_DEF_ID)) {
            metaDef = new MetaDef();
        } else {
            metaDef = helper.getMetaDef(ctx);
        }

        HashSet<String> attributesSet = Arrays.stream(attributes).collect(Collectors.toCollection(HashSet::new));
        ArrayList<String> acceptedAttributeNames = new ArrayList<>();
        ArrayList<MetaDef.AttributeDefinition> acceptedAttributes = new ArrayList<>();
        List<MetaDef.AttributeDefinition> attributeDefinitions = metaDef.getAttributeList();
        for (MetaDef.AttributeDefinition attributeDefinition : attributeDefinitions) {
            if (attributesSet.contains(attributeDefinition.getName())) {
                acceptedAttributes.add(attributeDefinition);
                acceptedAttributeNames.add(attributeDefinition.getName());
                attributesSet.remove(attributeDefinition.getName());
            }
        }
        if (attributesSet.size() > 0) {
            String[] attributesSetArray = attributesSet.toArray(new String[0]);
            if (attributesSetArray.length == 1)
                return helper.createReturnValue("400", "The attribute "
                        + attributesSetArray[0] + " is not defined");
            else {
                StringBuilder result = new StringBuilder("The attributes ");
                result.append(attributesSetArray[0]);
                for (int i = 1; i < attributesSetArray.length; i++) {
                    result.append(", ");
                    result.append(attributesSetArray[i]);
                }
                result.append(" are not defined");
                return helper.createReturnValue("400", result.toString());
            }
        }

        metaDef.addAssetDefinition(name, acceptedAttributeNames, acceptedAttributes);
        helper.putState(ctx, META_DEF_ID, metaDef);

        return helper.createSuccessReturnValue(metaDef.toJSON());
    }

    /**
     * Adds an unit to the meta def
     * If no meta def exists, it will be created
     *
     * @param ctx  the Hyperledger context object
     * @param unit the name of the unit
     * @return the meta def
     */
    @Transaction()
    public String addUnit(Context ctx, String unit) {

        MetaDef metaDef;
        if (!helper.objectExists(ctx, META_DEF_ID)) metaDef = new MetaDef();
        else metaDef = helper.getMetaDef(ctx);

        if (metaDef.getUnitList().contains(unit))
            return helper.createReturnValue("400", "The unit " + unit + " is already defined");

        metaDef.addUnitToUnitList(unit);
        helper.putState(ctx, META_DEF_ID, metaDef);

        return helper.createSuccessReturnValue(metaDef.toJSON());
    }


    /* #endregion */

    /* #region META objects */

    /**
     * Creates a new object (Pass transient data, to create private attribute ("attribute":"value"))
     *
     * @param ctx         the Hyperledger context object
     * @param id          the id of the object
     * @param pdc         the private data collection where to store the private data (empty if no private data necessary)
     * @param definition  the definition of this object (defined in the MetaDef)
     * @param amount      the initial amount of this object
     * @param unit        the unit definition of this object
     * @param attributes  the names of all attributes (defined in the MetaDef)
     * @param attrValues  the values of this object corresponding the attribute names
     * @return the object
     */
    @Transaction()
    public String createAsset(Context ctx, String id, String pdc, String definition, String amount,
                               String unit, String[] attributes, String[] attrValues) {

        if (helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " already exists");

        MetaDef metaDef = helper.getMetaDef(ctx);

        List<MetaDef.AssetDefinition> assetDefinitions = metaDef.getAssetDefinitions();
        MetaDef.AssetDefinition productDefinition = null;
        for (MetaDef.AssetDefinition assetDefinition : assetDefinitions)
            if (assetDefinition.getName().equalsIgnoreCase(definition)) {
                productDefinition = assetDefinition;
            }
        if (productDefinition == null)
            return helper.createReturnValue("400", "The product name " + definition + " is not defined");

        if (!metaDef.getUnitList().contains(unit))
            return helper.createReturnValue("400", "The unit " + unit + " is not defined");

        double amountDouble;
        try {
            amountDouble = Double.parseDouble(amount);
        } catch (Exception e) {
            return helper.createReturnValue("400", "The amount " + amount + " is not a double.");
        }

        // check attributes
        PrivateMetaObject privateMetaObject = null;
        Map<String, byte[]> transientData = ctx.getStub().getTransient();
        boolean isPdc;
        if (transientData.size() != 0) {
            if (pdc.equals(""))
                return helper.createReturnValue("400",
                        "Please select a private data collection to store the private data");
            isPdc = true;
        } else {
            isPdc = false;
        }

        Set<String> privateAttributes = transientData.keySet();
        ArrayList<String> attributeNames = new ArrayList<>(Arrays.asList(attributes));
        ArrayList<String> attributeValues = new ArrayList<>(Arrays.asList(attrValues));
        List<MetaDef.AttributeDefinition> allowedAttr = productDefinition.getAttributes();
        List<MetaDef.AttributeDefinition> acceptedAttr = new ArrayList<>();
        List<String> acceptedValues = new ArrayList<>();
        for (MetaDef.AttributeDefinition allowedDefinition : allowedAttr) {
            if (attributeNames.contains(allowedDefinition.getName())) {
                if (isPdc && privateAttributes.contains(allowedDefinition.getName()))
                    return helper.createReturnValue("400", "The attribute "
                            + allowedDefinition.getName() + " is defined as a public and private attribute.");
                int index = attributeNames.indexOf(allowedDefinition.getName());
                if (index < 0)
                    continue;
                acceptedAttr.add(allowedDefinition);
                acceptedValues.add(attributeValues.get(index));
                attributeValues.remove(index);
                attributeNames.remove(index);
            } else if (isPdc && privateAttributes.contains(allowedDefinition.getName())) {
                String value = new String(transientData.get(allowedDefinition.getName()), StandardCharsets.UTF_8);
                if (privateMetaObject == null)
                    privateMetaObject = new PrivateMetaObject();
                try {
                    privateMetaObject.addAttribute(allowedDefinition.getName(), allowedDefinition.getVersion(), value, allowedDefinition.getDataType());
                } catch (NumberFormatException e) {
                    return helper.createReturnValue("400", "Parsing Error: Could not parse the number");
                } catch (JsonSyntaxException e) {
                    return helper.createReturnValue("400", "Parsing Error: Could not parse the Json Array");
                }
                privateAttributes.remove(allowedDefinition.getName());
            } else
                return helper.createReturnValue("400", "The attribute "
                        + allowedDefinition.getName() + " is missing");
        }
        if (attributeNames.size() > 0) {
            if (attributeNames.size() == 1)
                return helper.createReturnValue("400", "The attribute "
                        + attributeNames.get(0) + " is not defined");
            else {
                StringBuilder result = new StringBuilder("The attributes ");
                result.append(attributeNames.get(0));
                for (int i = 1; i < attributeNames.size(); i++) {
                    result.append(", ");
                    result.append(attributeNames.get(i));
                }
                result.append(" are not defined");
                return helper.createReturnValue("400", result.toString());
            }
        }
        if (privateAttributes.size() > 0) {
            String[] privateAttributesArray = privateAttributes.toArray(new String[0]);
            if (privateAttributes.size() == 1)
                return helper.createReturnValue("400", "The attribute "
                        + privateAttributesArray[0] + " is not defined");
            else {
                StringBuilder result = new StringBuilder("The attributes ");
                result.append(attributeNames.get(0));
                for (int i = 1; i < privateAttributesArray.length; i++) {
                    result.append(", ");
                    result.append(privateAttributesArray[i]);
                }
                result.append(" are not defined");
                return helper.createReturnValue("400", result.toString());
            }
        }

        isPdc = isPdc && privateMetaObject != null;

        // attribute check end

        if (isPdc)
            helper.putPrivateData(ctx, pdc, id + PDC_STRING, privateMetaObject);

        String timeStamp = ctx.getStub().getTxTimestamp().toString();
        try {
            MetaObject metaObject = new MetaObject(isPdc ? pdc : "", productDefinition.getName(),
                    productDefinition.getVersion(), amountDouble, unit, acceptedAttr,
                    acceptedValues.toArray(new String[0]), timeStamp, ctx.getClientIdentity().getMSPID());
            metaObject.setKey(id);
            helper.putState(ctx, id, metaObject);

            return helper.createSuccessReturnValue(metaObject.toJSON());
        } catch (NumberFormatException e) {
            return helper.createReturnValue("400", "Parsing Error: Could not parse the number");
        } catch (JsonSyntaxException e) {
            return helper.createReturnValue("400", "Parsing Error: Could not parse the Json Array");
        }
    }

    /**
     * Deletes the object
     *
     * @param ctx the Hyperledger context object
     * @param id  the id to delete
     * @return 200, if object was deleted; 400, if the object does not exist
     */
    @Transaction()
    public String deleteAsset(Context ctx, String id) {
        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");
        MetaObject metaObject = helper.getMetaObject(ctx, id);
        for (String pdc : metaObject.getPrivateDataCollection()) {
            deletePrivateAttributes(ctx, id + PDC_STRING, pdc);
        }
        ctx.getStub().delState(id);
        return helper.createReturnValue("200", "The object with the key " + id + " was deleted");
    }

    /**
     * Reads an object
     *
     * @param ctx the hyperledger context object
     * @param id  the id of the object
     * @return the object
     */
    @Transaction()
    public String getAsset(Context ctx, String id) {

        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);

        ArrayList<MetaAttribute<?>> pdcList = new ArrayList<>();

        for (String pdc : metaObject.getPrivateDataCollection()) {
            PrivateMetaObject privateMetaObject = helper.getPrivateMetaObject(ctx, pdc, id + PDC_STRING);
            pdcList.addAll(privateMetaObject.getAttributes());
        }
        JsonObject returnValue = metaObject.toJSON();
        returnValue.add("privateData", new Gson().fromJson(new Gson().toJson(pdcList.toArray()),
                JsonArray.class));

        return helper.createSuccessReturnValue(returnValue);
    }

     /**
     * Returns the JSON-encoding of a List of MetaObjects representing the history of a specific key.
     *
     * @param ctx the hyperledger context object
     * @param id  the id of the object
     * @return the JSON string object
     */
    @Transaction()
    public String getAssetHistoryList(Context ctx, String id) {
        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");

        
        ArrayList<MetaObject> historyForObject = helper.getObjectHistoryFromKey(ctx, id);
        JsonArray jsonArray = new JsonArray();
        Iterator<MetaObject> it = historyForObject.iterator();
        while(it.hasNext()) {
            jsonArray.add(it.next().toJSON());
        }
        //String returnValue = new Gson().toJson(historyForObject, JsonArray.class);
        return helper.createSuccessReturnValue(jsonArray);
    }

    /**
     * Query the local state database (Couch DB query indexes)
     *
     * @param ctx the Hyperledger context object
     * @param query the query string
     * @return a list of all objects that fulfill the condition
     */
    @Transaction
    public String selectAsset(Context ctx, String query) {
        QueryResultsIterator<KeyValue> result = ctx.getStub().getQueryResult(query);
        Iterator<KeyValue> it = result.iterator();
        JsonArray jsonArray = new JsonArray();
        while (it.hasNext()) {
            jsonArray.add(new Gson().fromJson(new String(it.next().getValue(), StandardCharsets.UTF_8), JsonObject.class));
        }
        return helper.createSuccessReturnValue(jsonArray);
    }

    /**
     * Checks if an object to the given id exists
     *
     * @param ctx the Hyperledger context object
     * @param id  the id to check
     * @return true, if the object exists
     */
    @Transaction
    public String containsAsset(Context ctx, String id) {
        byte[] buffer = ctx.getStub().getState(id);
        return helper.createSuccessReturnValue(buffer != null && buffer.length > 0);
    }

    /**
     * Deletes the private object
     *
     * @param ctx the Hyperledger context object
     * @param id  the id to delete
     * @param pdc the private data collection where to find the object
     * @return 200, if object was deleted; 400, if the object does not exist (or we are not allowed to delete it)
     */
    @Transaction()
    public String deletePrivateAttributes(Context ctx, String id, String pdc) {

        if (!helper.privateObjectExists(ctx, id, pdc))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist in the private data collection " + pdc);

        ctx.getStub().delPrivateData(pdc, id);

        return helper.createReturnValue("200", "The object with the key " + id + " was deleted from the private data collection " + pdc);
    }

    /**
     * Checks if a private object to the given id exists
     *
     * @param ctx the Hyperledger context object
     * @param id  the asset id to check
     * @param pdc the private data collection to check
     * @return true, if the private object exists (and we are allowed to see it)
     */
    @Transaction()
    public String hasPrivateAttributes(Context ctx, String id, String pdc) {
        byte[] buffer1 = ctx.getStub().getState(id);
        byte[] buffer2 = ctx.getStub().getPrivateDataHash(pdc, id);
        boolean hasPrivateAttributes = buffer2 != null && buffer2.length > 0;
        if(!(buffer1 != null && buffer1.length > 0 && hasPrivateAttributes)) {
            helper.createReturnValue("400", "Asset does not exist.");
        }
        return helper.createSuccessReturnValue(hasPrivateAttributes);
    }

    /**
     * Sets the receiver
     *
     * @param ctx      the hyperledger context object
     * @param id       the id of the object
     * @param receiver the receiver to set
     * @return the object
     */
    @Transaction()
    public String setReceiver(Context ctx, String id, String receiver) {

        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);

        if (!metaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID()))
            return helper.createReturnValue("400", "You (" + ctx.getClientIdentity().getMSPID() + ") are not the actual owner");

        metaObject.setReceiver(receiver);
        helper.putState(ctx, id, metaObject);

        return helper.createSuccessReturnValue(metaObject.toJSON());
    }

    /**
     * Changes the owner
     *
     * @param ctx the hyperledger context object
     * @param id  the id of the object
     * @return the object
     */
    @Transaction()
    public String setOwner(Context ctx, String id) {

        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);
        String newOwner = ctx.getClientIdentity().getMSPID();

        if (!metaObject.getReceiver().equals(newOwner))
            return helper.createReturnValue("400", "You (" + ctx.getClientIdentity().getMSPID() + ") are not the receiver");

        metaObject.setReceiver("");
        metaObject.addTsAndOwner(ctx.getStub().getTxTimestamp().toString(), newOwner);
        metaObject.setActualOwner(newOwner);
        helper.putState(ctx, id, metaObject);

        return helper.createSuccessReturnValue(metaObject.toJSON());
    }

    /**
     * Adds a predecessor
     *
     * @param ctx           the hyperledger context object
     * @param predecessorId the id of the predecessor
     * @param id            the id of the successor
     * @param amountDif     the amount that was transferred
     * @return the successor object
     */
    @Transaction()
    public String addPredecessor(Context ctx, String predecessorId, String id, String amountDif, String addAmount) {

        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");

        if (!helper.objectExists(ctx, predecessorId))
            return helper.createReturnValue("400", "The object with the key " + predecessorId + " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);
        if (!metaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID()))
            return helper.createReturnValue("400", "You are not the owner of " + id);

        MetaObject preMetaObject = helper.getMetaObject(ctx, predecessorId);
        if (!preMetaObject.getActualOwner().equals(ctx.getClientIdentity().getMSPID()))
            return helper.createReturnValue("400", "You are not the owner of " + predecessorId);

        preMetaObject.addSuccessor(id, amountDif.substring(1) + " " + preMetaObject.getUnit());
        preMetaObject.addAmount(Double.parseDouble(amountDif));

        if (preMetaObject.getAmount() < 0.0) return helper.createReturnValue("400", "The amount is lower than zero");

        metaObject.addAmount(Double.parseDouble(addAmount));

        helper.putState(ctx, predecessorId, preMetaObject);

        metaObject.addPredecessor(predecessorId, amountDif.substring(1) + " " + preMetaObject.getUnit() + " " + preMetaObject.getProductName());
        helper.putState(ctx, id, metaObject);

        return helper.createSuccessReturnValue(metaObject.toJSON());
    }

    /**
     * Updates an object
     *
     * @param ctx       the hyperledger context object
     * @param id        the id of the object
     * @param attrName  the name of the attribute
     * @param attrValue the new value of this attribute
     * @return the object
     */
    @Transaction()
    public String setAttribute(Context ctx, String id, String[] attrName, String[] attrValue) {

        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);

        MetaDef metaDef = helper.getMetaDef(ctx);

        ArrayList<String> attributeList = new ArrayList<>(Arrays.asList(attrName));
        ArrayList<String> attributeValues = new ArrayList<>(Arrays.asList(attrValue));
        List<MetaDef.AttributeDefinition> allowedAttr = metaDef.getAttributesByAssetNameAndVersion(metaObject.getProductName(),
                metaObject.getProductVersion());

        if (allowedAttr != null) {
            for (MetaDef.AttributeDefinition attributeDefinition : allowedAttr) {
                if (attributeList.contains(attributeDefinition.getName())) {
                    int index = attributeList.indexOf(attributeDefinition.getName());
                    try {
                        metaObject.addAttribute(attributeDefinition.getName(), attributeDefinition.getVersion(),
                                attributeValues.get(index), attributeDefinition.getDataType());
                    } catch (NumberFormatException e) {
                        return helper.createReturnValue("400", "Parsing Error: Could not parse the number");
                    } catch (JsonSyntaxException e) {
                        return helper.createReturnValue("400", "Parsing Error: Could not parse the Json Array");
                    }
                    attributeValues.remove(index);
                    attributeList.remove(attributeDefinition.getName());
                }
            }
        }
        if (attributeList.size() > 0) {
            if (attributeList.size() == 1)
                return helper.createReturnValue("400", "The attribute "
                        + attributeList.get(0) + " is not defined");
            else {
                StringBuilder result = new StringBuilder("The attributes ");
                result.append(attributeList.get(0));
                for (int i = 1; i < attributeList.size(); i++) {
                    result.append(", ");
                    result.append(attributeList.get(i));
                }
                result.append(" are not defined");
                return helper.createReturnValue("400", result.toString());
            }
        }

        helper.putState(ctx, id, metaObject);

        return helper.createSuccessReturnValue(metaObject.toJSON());
    }

    /**
     * Updates a private attribute (Transient data("attribute":"value"))
     *
     * @param ctx the hyperledger context object
     * @param id  the id of the object
     * @param pdc the private data collection to store the private data
     * @return the object
     */
    @Transaction()
    public String setPrivateAttribute(Context ctx, String id, String pdc) {

        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);

        PrivateMetaObject privateMetaObject = helper.privateObjectExists(ctx, id + PDC_STRING, pdc) ?
                helper.getPrivateMetaObject(ctx, pdc, id + PDC_STRING) : new PrivateMetaObject();

        MetaDef metaDef = helper.getMetaDef(ctx);

        Map<String, byte[]> transientData = ctx.getStub().getTransient();
        if (transientData.size() == 0)
            return helper.createReturnValue("400", "No transient data passed");
        if (pdc.equals(""))
            return helper.createReturnValue("400",
                    "Please select a private data collection to store the private data");

        Set<String> privateAttributes = transientData.keySet();
        List<MetaDef.AttributeDefinition> allowedAttr = metaDef.getAttributesByAssetNameAndVersion(metaObject.getProductName(),
                metaObject.getProductVersion());
        for (MetaDef.AttributeDefinition attributeDefinition : allowedAttr) {
            if (privateAttributes.contains(attributeDefinition.getName())) {
                String value = new String(transientData.get(attributeDefinition.getName()), StandardCharsets.UTF_8);
                if (privateMetaObject == null)
                    privateMetaObject = new PrivateMetaObject();
                try {
                    privateMetaObject.addAttribute(attributeDefinition.getName(), attributeDefinition.getVersion(), value,
                            attributeDefinition.getDataType());
                } catch (NumberFormatException e) {
                    return helper.createReturnValue("400", "Parsing Error: Could not parse the number");
                } catch (JsonSyntaxException e) {
                    return helper.createReturnValue("400", "Parsing Error: Could not parse the Json Array");
                }
                privateAttributes.remove(attributeDefinition.getName());
            }
        }

        if (!metaObject.getPrivateDataCollection().contains(pdc)) {
            metaObject.addPrivateDataCollection(pdc);
        }

        helper.putPrivateData(ctx, pdc, id + PDC_STRING, privateMetaObject);
        return helper.createSuccessReturnValue(privateMetaObject.toJSON());
    }

    /* #endregion */

    /* #region alarm handling */

    /**
     * Activates the alarm (All successors will be informed)
     *
     * @param ctx the hyperledger context object
     * @param id  the id of the object
     * @return the object
     */
    @Transaction()
    public String activateAlarm(Context ctx, String id) {

        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);
        metaObject.setAlarmFlag(true);
        helper.putState(ctx, id, metaObject);

        ArrayList<MetaObject.Tuple<String, String>> successors = metaObject.getSuccessor();

        for (MetaObject.Tuple<String, String> suc : successors) {
            MetaObject sucMetaObject = helper.getMetaObject(ctx, suc.x);
            sucMetaObject.setAlarmFlag(true);
            helper.putState(ctx, suc.x, metaObject);
        }
        helper.emitEvent(ctx, "alarm_activated", metaObject.toString().getBytes());

        return helper.createSuccessReturnValue(metaObject.toJSON());
    }

    /**
     * Deactivates the alarm (All successors will be informed)
     *
     * @param ctx the hyperledger context object
     * @param id  the id of the object
     * @return the object
     */
    @Transaction()
    public String deactivateAlarm(Context ctx, String id) {

        if (!helper.objectExists(ctx, id))
            return helper.createReturnValue("400", "The object with the key " + id + " does not exist");

        MetaObject metaObject = helper.getMetaObject(ctx, id);
        metaObject.setAlarmFlag(false);
        helper.putState(ctx, id, metaObject);

        ArrayList<MetaObject.Tuple<String, String>> successors = metaObject.getSuccessor();

        for (MetaObject.Tuple<String, String> suc : successors) {
            MetaObject sucMetaObject = helper.getMetaObject(ctx, suc.x);
            sucMetaObject.setAlarmFlag(false);
            helper.putState(ctx, suc.x, metaObject);
        }

        return helper.createSuccessReturnValue(metaObject.toJSON());
    }

    @Deprecated
    @Transaction
    public String queryChaincodeByQueryString(Context ctx, String queryString) {
        return this.selectAsset(ctx, queryString);
    }

    @Deprecated
    @Transaction
    public String objectExists(Context ctx, String id) {
        return this.containsAsset(ctx, id);
    }

    @Deprecated
    @Transaction()
    public String privateObjectExists(Context ctx, String id, String pdc) {
        return this.hasPrivateAttributes(ctx, id, pdc);
    }

    @Deprecated
    @Transaction()
    public String deleteObject(Context ctx, String id) {
        return this.deleteAsset(ctx, id);
    }

    @Deprecated
    @Transaction()
    public String deletePrivateObject(Context ctx, String id, String pdc) {
        return this.deletePrivateAttributes(ctx, id, pdc);
    }

    @Deprecated
    @Transaction()
    public String META_getAttributesOfProductWithVersion(Context ctx, String product, String version) {
        return this.getAttributeDefinitionsOfAsset(ctx, product, version);
    }

    @Deprecated
    @Transaction()
    public String META_getDataTypeOfAttributeWithVersion(Context ctx, String attribute, String version) {
        return this.getDataTypeOfAttributeDefinition(ctx, attribute, version);
    }

    @Deprecated
    @Transaction()
    public String META_readMetaDefOfProduct(Context ctx, String product) {
        return this.getAssetDefinition(ctx, product);
    }

    @Deprecated
    @Transaction()
    public String META_readMetaDef(Context ctx) {
        return this.getDefinition(ctx);
    }

    @Deprecated
    @Transaction()
    public String META_deleteProduct(Context ctx, String name) {
        return this.removeAssetDefinition(ctx, name);
    }

    @Deprecated
    @Transaction()
    public String META_addAttributeDefinition(Context ctx, String attribute, String dataType) {
        return this.putAttributeDefinition(ctx, attribute, dataType);
    }

    @Deprecated
    @Transaction()
    public String META_addProductDefinition(Context ctx, String productName, String[] attributes) {
        return this.putAssetDefinition(ctx, productName, attributes);
    }

    @Deprecated
    @Transaction()
    public String META_addUnit(Context ctx, String unit) {
        return this.addUnit(ctx, unit);
    }

    @Deprecated
    @Transaction()
    public String createObject(Context ctx, String id, String pdc, String productName, String amount,
                               String unit, String[] attributes, String[] attrValues) {
        return this.createAsset(ctx, id, pdc, productName, amount, unit, attributes, attrValues);
    }

    @Deprecated
    @Transaction()
    public String readObject(Context ctx, String id) {
        return this.getAsset(ctx, id);
    }

    @Deprecated
    @Transaction()
    public String changeOwner(Context ctx, String id) {
        return this.setOwner(ctx, id);
    }

    @Deprecated
    @Transaction()
    public String updateAttribute(Context ctx, String id, String[] attrName, String[] attrValue) {
        return this.setAttribute(ctx, id, attrName, attrValue);
    }

    @Deprecated
    @Transaction()
    public String updatePrivateAttribute(Context ctx, String id, String pdc) {
        return this.setPrivateAttribute(ctx, id, pdc);
    }

}

