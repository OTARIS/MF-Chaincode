package org.example;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

public class Shipment {

    @Property
    String senderName;

    @Property
    String senderAddress;

    @Property
    String senderZipCode;

    @Property
    String senderCity;

    @Property
    String recipientName;

    @Property
    String recipientAddress;

    @Property
    String recipientZipCode;

    @Property
    String recipientCity;

    @Property
    String postage;

    @Property
    String[] packagingType;

    @Property
    String[] content;

    @Property
    String[] weight;

    @Property
    String[] length;

    @Property
    String[] width;

    @Property
    String[] height;

    @Property
    String status;

    public Shipment(String senderName,
                    String senderAddress,
                    String senderZipCode,
                    String senderCity,
                    String recipientName,
                    String recipientAddress,
                    String recipientZipCode,
                    String recipientCity,
                    String postage,
                    String[] packagingType,
                    String[] content,
                    String[] weight,
                    String[] length,
                    String[] width,
                    String[] height,
                    String status){
        this.senderName = senderName;
        this.senderAddress = senderAddress;
        this.senderZipCode = senderZipCode;
        this.senderCity = senderCity;
        this.recipientName = recipientName;
        this.recipientAddress = recipientAddress;
        this.recipientZipCode = recipientZipCode;
        this.recipientCity = recipientCity;
        this.postage = postage;
        this.packagingType = packagingType;
        this.content = content;
        this.weight = weight;
        this.length = length;
        this.width = width;
        this.height = height;
        this.status = status;
    }

    public Shipment(){
    }

    public void setContent(String[] content) {
        this.content = content;
    }

    public void setLength(String[] length) {
        this.length = length;
    }

    public void setPackagingType(String[] packagingType) {
        this.packagingType = packagingType;
    }

    public void setPostage(String postage) {
        this.postage = postage;
    }

    public void setRecipientAddress(String recipientAddress) {
        this.recipientAddress = recipientAddress;
    }

    public void setRecipientCity(String recipientCity) {
        this.recipientCity = recipientCity;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public void setHeight(String[] height) {
        this.height = height;
    }

    public void setRecipientZipCode(String recipientZipCode) {
        this.recipientZipCode = recipientZipCode;
    }

    public void setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
    }

    public void setSenderCity(String senderCity) {
        this.senderCity = senderCity;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public void setSenderZipCode(String senderZipCode) {
        this.senderZipCode = senderZipCode;
    }

    public void setWeight(String[] weight) {
        this.weight = weight;
    }

    public void setWidth(String[] width) {
        this.width = width;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String toJSONString() {
        return new JSONObject(this).toString();
    }

    public JSONObject toJSON() {
        return new JSONObject(this);
    }

    public static Shipment fromJSONString(String json) {
        Shipment shipment = new Shipment();

        shipment.setSenderName(new JSONObject(json).getString("senderName"));
        shipment.setSenderAddress(new JSONObject(json).getString("senderAddress"));
        shipment.setSenderZipCode(new JSONObject(json).getString("senderZipCode"));
        shipment.setSenderCity(new JSONObject(json).getString("senderCity"));
        shipment.setRecipientName(new JSONObject(json).getString("recipientName"));
        shipment.setRecipientAddress(new JSONObject(json).getString("recipientAddress"));
        shipment.setRecipientZipCode(new JSONObject(json).getString("recipientZipCode"));
        shipment.setRecipientCity(new JSONObject(json).getString("recipientCity"));
        shipment.setPostage(new JSONObject(json).getString("postage"));
        shipment.setStatus(new JSONObject(json).getString("status"));

        String packagingTypeString = new JSONObject(json).get("packagingType").toString();
        String[] packagingMap = new Gson().fromJson(
                packagingTypeString, new TypeToken<String[]>() {}.getType()
        );
        shipment.setPackagingType(packagingMap);

        String contentString = new JSONObject(json).get("content").toString();
        String[] contentMap = new Gson().fromJson(
                contentString, new TypeToken<String[]>() {}.getType()
        );
        shipment.setContent(contentMap);

        String weightString = new JSONObject(json).get("weight").toString();
        String[] weightMap = new Gson().fromJson(
                weightString, new TypeToken<String[]>() {}.getType()
        );
        shipment.setWeight(weightMap);

        String lengthString = new JSONObject(json).get("length").toString();
        String[] lengthMap = new Gson().fromJson(
                lengthString, new TypeToken<String[]>() {}.getType()
        );
        shipment.setLength(lengthMap);

        String widthString = new JSONObject(json).get("width").toString();
        String[] widthMap = new Gson().fromJson(
                widthString, new TypeToken<String[]>() {}.getType()
        );
        shipment.setWidth(widthMap);

        String heightString = new JSONObject(json).get("height").toString();
        String[] heightMap = new Gson().fromJson(
                heightString, new TypeToken<String[]>() {}.getType()
        );
        shipment.setHeight(heightMap);

        return  shipment;
    }



}

