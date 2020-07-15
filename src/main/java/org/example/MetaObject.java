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
    boolean alarmFlag = false; //TODO Implementierung

    @Property()
    String productName = "";

    @Property()
    String receiver = "";

    @Property()
    ArrayList<String> predecessor = new ArrayList<>();

    @Property()
    ArrayList<String> successor = new ArrayList<>();

    @Property()
    String actualOwner = "";

    @Property()
    HashMap<String, String> tsAndOwner = new HashMap<>();

    @Property()
    HashMap<String, String> attributes = new HashMap<>();

    public MetaObject(){
    }

    public MetaObject(String productName, String[] attrNames, String[] attrValues, String timeStamp, String owner){
        this.productName = productName;
        for (int i = 0; i < attrNames.length; i++){
            attributes.put(attrNames[i], attrValues[i]);
        }
        tsAndOwner.put(timeStamp, owner);
        actualOwner = owner;
        
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public boolean getAlarmFlag() {
        return alarmFlag;
    }

    public void setAlarmFlag(boolean alarmFlag){
        this.alarmFlag = alarmFlag;
    }

    public HashMap<String, String> getAttributes() {
        return attributes;
    }

    public void setAttributes(HashMap<String, String> attr) {
        attributes = attr;
    }

    public void addAttribute(String attrName, String attrValue) {
        attributes.put(attrName, attrValue);
    }

    public void deleteAttribute(String attrName) {
        attributes.remove(attrName);
    }

    public void addTsAndOwner(String timeStamp, String owner){
        tsAndOwner.put(timeStamp, owner);
    }

    public void setTsAndOwner(HashMap<String, String> owner){
        tsAndOwner = owner;
    }

    public void setActualOwner(String owner){
        actualOwner = owner;
    }

    public String getActualOwner(){
        return actualOwner;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setSuccessor(ArrayList<String> successor) {
        this.successor = successor;
    }

    public void addSuccessor(String successor){
        this.successor.add(successor);
    }

    public ArrayList<String> getSuccessor() {
        return successor;
    }

    public void setPredecessor(ArrayList<String> predecessor) {
        this.predecessor = predecessor;
    }

    public void addPredecessor(String predecessor){
        this.predecessor.add(predecessor);
    }

    public ArrayList<String> getPredecessor() {
        return predecessor;
    }

    @Override
    public String toString(){
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        sb.append("Name: " + productName + "\n");
        sb.append("Alarm flag: " + alarmFlag + "\n");
        sb.append("Receiver: " + receiver + "\n");
        sb.append("Actual Owner: " + actualOwner+ "\n");
        sb.append("Attributes: " + attributes.toString() + "\n");
        sb.append("TimeStamp and Owner: " + tsAndOwner.toString() + "\n");
        sb.append("Predecessor: " + predecessor.toString() + "\n");
        sb.append("Successor: " + successor.toString() + "\n");       
        return sb.toString();
    }

    public String toJSONString() {      
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static MetaObject fromJSONString(String json) {
        MetaObject metaObject = new MetaObject();
        String name = new JSONObject(json).getString("dataName");
        metaObject.setProductName(name);

        boolean alarm = new JSONObject(json).getBoolean("alarmFlag");
        metaObject.setAlarmFlag(alarm);

        String owner = new JSONObject(json).getString("actualOwner");
        metaObject.setActualOwner(owner);

        String rec = new JSONObject(json).getString("receiver");
        metaObject.setReceiver(rec);

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

        String predecessorString = new JSONObject(json).get("predecessor").toString();
        ArrayList<String> predecessorMap = new Gson().fromJson(
            predecessorString, new TypeToken<ArrayList<String>>() {}.getType()
        );
        metaObject.setPredecessor(predecessorMap);

        String successorString = new JSONObject(json).get("successor").toString();
        ArrayList<String> successorMap = new Gson().fromJson(
            successorString, new TypeToken<ArrayList<String>>() {}.getType()
        );
        metaObject.setSuccessor(successorMap);

        return metaObject;
    }
}