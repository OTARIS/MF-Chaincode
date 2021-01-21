package org.example;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import java.util.HashMap;

public class Shipment {

    @Property
    String interchangeNumber;

    @Property
    String sender;

    @Property
    String recipient;

    @Property
    String timestamp;

    @Property
    String pickupTime;

    @Property
    String deliverTime;

    @Property
    String status;

    @Property
    HashMap<String, String> items = new HashMap<>();

    public Shipment(String sender, String recipient, String timestamp, String pickupTime, String deliverTime, HashMap<String, String> items){
        this.sender = sender;
        this.recipient = recipient;
        this.timestamp = timestamp;
        this.pickupTime = pickupTime;
        this.deliverTime = deliverTime;
        this.items = items;
    }

    public Shipment(String interchangeNumber, String sender, String recipient, String timestamp, String pickupTime, String deliverTime){
        this.sender = sender;
        this.recipient = recipient;
        this.timestamp = timestamp;
        this.pickupTime = pickupTime;
        this.deliverTime = deliverTime;
        this.interchangeNumber = interchangeNumber;
    }

    public Shipment(){
    }

    public String getInterchangeNumber() {
        return interchangeNumber;
    }

    public void setInterchangeNumber(String interchangeNumber) {
        this.interchangeNumber = interchangeNumber;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getSender() {
        return sender;
    }

    public void setRecipient(String recipient) {
        this.recipient = recipient;
    }

    public String getRecipient() {
        return recipient;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setPickupTime(String pickupTime) {
        this.pickupTime = pickupTime;
    }

    public String getPickupTime() {
        return pickupTime;
    }

    public void setDeliverTime(String deliverTime) {
        this.deliverTime = deliverTime;
    }

    public String getDeliverTime() {
        return deliverTime;
    }


    public void setItems(HashMap<String, String> items) {
        this.items = items;
    }

    public HashMap<String, String> getItems() {
        return items;
    }

    public void addItem(String itemName, String itemAmountAndUnit){
        items.put(itemName, itemAmountAndUnit);
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean updateAttribute(String attribute, String attrValue){
        if(attribute.equals("recipient")){
            setRecipient(attrValue);
            return true;
        }
        else if (attribute.equals("pickupTime")){
            setPickupTime(attrValue);
            return true;
        }
        else if (attribute.equals("deliverTime")){
            setDeliverTime(attrValue);
            return true;
        }
        else if (attribute.equals("status")){
            setStatus(attrValue);
            return true;
        }
        else return false;
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public JSONObject toJSON() {
        return new JSONObject(this);
    }

    public static Shipment fromJSONString(String json) {
        Shipment shipment = new Shipment();

        shipment.setSender(new JSONObject(json).getString("sender"));
        shipment.setRecipient(new JSONObject(json).getString("recipient"));
        shipment.setTimestamp(new JSONObject(json).getString("timestamp"));
        shipment.setPickupTime(new JSONObject(json).getString("pickupTime"));
        shipment.setDeliverTime(new JSONObject(json).getString("deliverTime"));
        shipment.setStatus(new JSONObject(json).getString("status"));
        shipment.setInterchangeNumber(new JSONObject(json).getString("interchangeNumber"));

        String itemsString = new JSONObject(json).get("items").toString();
        HashMap<String, String> itemsMap = new Gson().fromJson(
                itemsString, new TypeToken<HashMap<String, String>>() {}.getType()
        );
        shipment.setItems(itemsMap);

        return  shipment;
    }



}

