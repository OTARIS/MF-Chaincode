package org.example;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import java.util.HashMap;

public class Order {

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
    HashMap<String, String> items = new HashMap<>();

    public Order(String sender, String recipient, String timestamp, String pickupTime, String deliverTime, HashMap<String, String> items){
        this.sender = sender;
        this.recipient = recipient;
        this.timestamp = timestamp;
        this.pickupTime = pickupTime;
        this.deliverTime = deliverTime;
        this.items = items;
    }

    public Order(String interchangeNumber, String sender, String recipient, String timestamp, String pickupTime, String deliverTime){
        this.sender = sender;
        this.recipient = recipient;
        this.timestamp = timestamp;
        this.pickupTime = pickupTime;
        this.deliverTime = deliverTime;
    }

    public Order(){
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

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public static Order fromJSONString(String json) {
        Order order = new Order();

        order.setSender(new JSONObject(json).getString("sender"));
        order.setRecipient(new JSONObject(json).getString("recipient"));
        order.setTimestamp(new JSONObject(json).getString("timestamp"));
        order.setPickupTime(new JSONObject(json).getString("pickupTime"));
        order.setDeliverTime(new JSONObject(json).getString("deliverTime"));
        order.setInterchangeNumber(new JSONObject(json).getString("interchangeNumber"));

        String itemsString = new JSONObject(json).get("intermediates").toString();
        HashMap<String, String> itemsMap = new Gson().fromJson(
                itemsString, new TypeToken<HashMap<String, String>>() {}.getType()
        );
        order.setItems(itemsMap);

        return  order;
    }



}

