package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

/**
 * A MetaObject is a generic definition of the object stored in the blockchain
 */

@DataType()
public class MetaObject {

    /**
     * The key to find the object in the ledger
     */
    @Property()
    String key = "";

    /**
     * The actual amount of this object
     */
    @Property()
    double amount = 0;

    /**
     * The unit belonging to the amount
     */
    @Property()
    String unit = "";

    /**
     * The alarm flag of this object (is there a problem wiht this objectt?)
     */
    @Property()
    boolean alarmFlag = false;

    /**
     * The product name (which has to be defined in the MetaDef) of this object
     */
    @Property()
    String productName = "";

    /**
     * The receiver of this object
     */
    @Property()
    String receiver = "";

    /**
     * The actual owner of this object
     */
    @Property()
    String actualOwner = "";

    /**
     * The list of all private data collections where private data corresponding to this object is stored
     */
    @Property()
    ArrayList<String> privateDataCollection = new ArrayList<>();

    /**
     * The list of keys of all predecesors of this object
     */
    @Property()
    HashMap<String, String> predecessor = new HashMap<>();

    /**
     * The list of keys of all successors of this object
     */
    @Property()
    HashMap<String, String> successor = new HashMap<>();  

    /**
     * The list of timestamp and all owners of this object
     */
    @Property()
    HashMap<String, String> tsAndOwner = new HashMap<>();

    /**
     * The list of attributes defined in this object
     */
    @Property()
    HashMap<String, String> attributes = new HashMap<>();

    /**
     * Empty class constructor
     */
    public MetaObject(){
    }

    /**
     * Class constructor
     * 
     * @param pdc the private data collection where to store the private data (empty if no private data necessary)
     * @param productName the product name of this objects (defined in the MetaDef)
     * @param amount the inital amount of this object
     * @param unit the unit definiton of this object
     * @param attrNames the names of all attributes (defined in the MetaDef)
     * @param attrValues the values of this object corresponding the attribute names
     * @param timeStamp the time of the creation (auto generated)
     * @param owner the inital owner of this object
     */
    public MetaObject(String pdc, String productName, double amount, String unit, String[] attrNames, String[] attrValues, String timeStamp, String owner){
        this.productName = productName;
        if (!pdc.equals("") && !pdc.equals("null")){
            this.privateDataCollection.add(pdc);
        }
        for (int i = 0; i < attrNames.length; i++){
            attributes.put(attrNames[i], attrValues[i]);
        }
        tsAndOwner.put(timeStamp, owner);
        actualOwner = owner;
        this.unit = unit;
        this.amount = amount;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key){
        this.key = key;
    }

    /**
     * @return the key where to find the object
     */
    public String getKey(){
        return key;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount){
        this.amount = amount;
    }

    /**
     * @return the actual amount
     */
    public double getAmount(){
        return amount;
    }

    /**
     * @param amount the amount to add
     */
    public void addAmount(double amount){
        this.amount += amount;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(String unit){
        this.unit = unit;
    }

    /**
     * @return the unit definiton
     */
    public String getUnit(){
        return unit;
    }

    /**
     * @return the alarmFlag
     */
    public boolean getAlarmFlag() {
        return alarmFlag;
    }

    /**
     * @param alarmFlag the alarm flag to set
     */
    public void setAlarmFlag(boolean alarmFlag){
        this.alarmFlag = alarmFlag;
    }

    /**
     * @return the product name
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @param productName the product name to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * @return the receiver
     */
    public String getReceiver() {
        return receiver;
    }

    /**
     * @param receiver the receiver to set
     */
    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    /**
     * @return the list of private data collections
     */
    public ArrayList<String> getPrivateDataCollection() {
        return privateDataCollection;
    }

    /**
     * @param pdc the private data collection to set
     */
    public void setPrivateDataCollection(ArrayList<String> pdc) {
        privateDataCollection = pdc;
    }

    /**
     * @param pdc the pdc to add
     */
    public void addPrivateDataCollection(String pdc) {
        privateDataCollection.add(pdc);
    }

    /**
     * @return the actual owner
     */
    public String getActualOwner(){
        return actualOwner;
    }

    /**
     * @param owner the actual owner to set
     */
    public void setActualOwner(String owner){
        actualOwner = owner;
    }

    /**
     * @return the map of predecessors
     */
    public HashMap<String, String> getPredecessor() {
        return predecessor;
    }

    /**
     * @param predecessor the map of predecessors to set
     */
    public void setPredecessor(HashMap<String, String> predecessor) {
        this.predecessor = predecessor;
    }

    /**
     * @param predecessor the predecessor to add
     * @param message the message corresponding to the predecessor (How much was processed)
     */
    public void addPredecessor(String predecessor, String message){
            this.predecessor.put(predecessor, message);
    }

    /**
     * @return the map of successors
     */
    public HashMap<String, String> getSuccessor() {
        return successor;
    }

    /**
     * @param successor the map of successors to set
     */
    public void setSuccessor(HashMap<String, String>successor) {
        this.successor = successor;
    }

    /**
     * @param successor the successor to add
     * @param message the message corresponding to the predecessor (How much was processed)
     */
    public void addSuccessor(String successor, String message){
            this.successor.put(successor, message);     
    }

    /**
     * @return the map of timestamp and owner
     */
    public HashMap<String, String> getTsAndOwner(){
        return tsAndOwner;
    }

    /**
     * @param owner the map of owners to set
     */
    public void setTsAndOwner(HashMap<String, String> owner){
        tsAndOwner = owner;
    }

    /**
     * @param timeStamp the timestamp to add
     * @param message the owner corresponding to the timestamp
     */
    public void addTsAndOwner(String timeStamp, String owner){
        tsAndOwner.put(timeStamp, owner);
    }

    /**
     * @return the map of attributes names and attribute values
     */
    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    /**
     * @param attr the map of attributes to set
     */
    public void setAttributes(HashMap<String, String> attr) {
        attributes = attr;
    }

    /**
     * @param attrName the attribute name to add
     * @param attrValue the attribute value to add
     */
    public void addAttribute(String attrName, String attrValue) {
        attributes.put(attrName, attrValue);
    }

    /**
     * @param map the map of all attributes to add
     */
    public void addAllAttributes(HashMap<String, String> map) {
        attributes.putAll(map);
    }

    /**
     * @param attrName the attribute to delete
     */
    public void deleteAttribute(String attrName) {
        attributes.remove(attrName);
    }

    /**
     * @return the object as a json string
     */
    public String toString(){
        return toJSONString();
    }

    /**
     * @return the object as a json string
     */
    public String toJSONString() {      
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    /**
     * Convert the json string of this object back to a MetaDef
     * 
     * @param json the json String of the object to decrypt
     * @return the decrypted object
     */
    public static MetaObject fromJSONString(String json) {

        MetaObject metaObject = new MetaObject();

        String key = new JSONObject(json).getString("key");
        metaObject.setKey(key);

        double amount = new JSONObject(json).getInt("amount");
        metaObject.setAmount(amount);

        String unit = new JSONObject(json).getString("unit");
        metaObject.setUnit(unit);

        boolean alarm = new JSONObject(json).getBoolean("alarmFlag");
        metaObject.setAlarmFlag(alarm);

        String name = new JSONObject(json).getString("productName");
        metaObject.setProductName(name);

        String rec = new JSONObject(json).getString("receiver");
        metaObject.setReceiver(rec);

        String pdcString = new JSONObject(json).get("privateDataCollection").toString();
        ArrayList<String> pdcMap = new Gson().fromJson(
            pdcString, new TypeToken<ArrayList<String>>() {}.getType()
        );
        metaObject.setPrivateDataCollection(pdcMap);

        String owner = new JSONObject(json).getString("actualOwner");
        metaObject.setActualOwner(owner);

        String predecessorString = new JSONObject(json).get("predecessor").toString();
        HashMap<String, String> predecessorMap = new Gson().fromJson(
            predecessorString, new TypeToken<HashMap<String, String>>() {}.getType()
        );
        metaObject.setPredecessor(predecessorMap);

        String successorString = new JSONObject(json).get("successor").toString();
        HashMap<String, String> successorMap = new Gson().fromJson(
            successorString, new TypeToken<HashMap<String, String>>() {}.getType()
        );
        metaObject.setSuccessor(successorMap);
       
        String attributesString = new JSONObject(json).get("attributes").toString();
        HashMap<String, String> attributesMap = new Gson().fromJson(
            attributesString, new TypeToken<HashMap<String, String>>() {}.getType()
        );
        metaObject.setAttributes(attributesMap);

        String tsAndOwnerString = new JSONObject(json).get("tsAndOwner").toString();
        HashMap<String, String> tsAndOwnerMap = new Gson().fromJson(
            tsAndOwnerString, new TypeToken<HashMap<String, String>>() {}.getType()
        );
        metaObject.setTsAndOwner(tsAndOwnerMap);

        return metaObject;
    }
}