package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

/*
A MetaObject is a generic definition of the object stored in the blockchain
*/

@DataType()
public class MetaObject {

    @Property()
    String key = "";

    @Property()
    double amount = 0;

    @Property()
    String unit = "";

    @Property()
    boolean alarmFlag = false;

    @Property()
    String productName = "";

    @Property()
    String receiver = "";

    @Property()
    String actualOwner = "";

    @Property()
    ArrayList<String> privateDataCollection = new ArrayList<>();

    @Property()
    HashMap<String, String> predecessor = new HashMap<>();

    @Property()
    HashMap<String, String> successor = new HashMap<>();  

    @Property()
    HashMap<String, String> tsAndOwner = new HashMap<>();

    @Property()
    HashMap<String, String> attributes = new HashMap<>();

    public MetaObject(){
    }

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

    //key
    public void setKey(String key){
        this.key = key;
    }

    public String getKey(){
        return key;
    }

    //amount
    public void setAmount(double amount){
        this.amount = amount;
    }

    public double getAmount(){
        return amount;
    }

    public void addAmount(double amount){
        this.amount += amount;
    }

    //unit
    public void setUnit(String unit){
        this.unit = unit;
    }

    public String getUnit(){
        return unit;
    }

    //alarmFlag
    public boolean getAlarmFlag() {
        return alarmFlag;
    }

    public void setAlarmFlag(boolean alarmFlag){
        this.alarmFlag = alarmFlag;
    }

    //productName
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    //receiver
    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    //pdc
    public ArrayList<String> getPrivateDataCollection() {
        return privateDataCollection;
    }

    public void setPrivateDataCollection(ArrayList<String> pdc) {
        privateDataCollection = pdc;
    }

    public void addPrivateDataCollection(String pdc) {
        privateDataCollection.add(pdc);
    }

    //actualOwner
    public String getActualOwner(){
        return actualOwner;
    }

    public void setActualOwner(String owner){
        actualOwner = owner;
    }

    //predecessor
    public HashMap<String, String> getPredecessor() {
        return predecessor;
    }

    public void setPredecessor(HashMap<String, String> predecessor) {
        this.predecessor = predecessor;
    }

    public void addPredecessor(String predecessor, String message){
            this.predecessor.put(predecessor, message);
    }

    //successor
    public HashMap<String, String> getSuccessor() {
        return successor;
    }

    public void setSuccessor(HashMap<String, String>successor) {
        this.successor = successor;
    }

    public void addSuccessor(String successor, String message){
            this.successor.put(successor, message);     
    }

    //tsAndOwner
    public HashMap<String, String> getTsAndOwner(){
        return tsAndOwner;
    }

    public void setTsAndOwner(HashMap<String, String> owner){
        tsAndOwner = owner;
    }

    public void addTsAndOwner(String timeStamp, String owner){
        tsAndOwner.put(timeStamp, owner);
    }

    //attributes
    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attr) {
        attributes = attr;
    }

    public void addAttribute(String attrName, String attrValue) {
        attributes.put(attrName, attrValue);
    }

    public void addAllAttributes(HashMap<String, String> map) {
        attributes.putAll(map);
    }

    public void deleteAttribute(String attrName) {
        attributes.remove(attrName);
    }

    public String toString(){
        return toJSONString();
    }

    //save and load
    public String toJSONString() {      
        Gson gson = new Gson();
        return gson.toJson(this);
    }

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