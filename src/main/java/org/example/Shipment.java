package org.example;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Arrays;

public class Shipment {

    @Property
    String senderName = "1";

    @Property
    String senderAddress = "2";

    @Property
    String senderZipCode = "3";

    @Property
    String senderCity = "4";

    @Property
    String recipientName = "5";

    @Property
    String recipientAddress = "6";

    @Property
    String recipientZipCode = "";

    @Property
    String recipientCity = "8";


    @Property
    ArrayList<String> packagingType = new ArrayList<>();

    @Property
    ArrayList<String> content = new ArrayList<>();

    @Property
    ArrayList<String> weight = new ArrayList<>();

    @Property
    ArrayList<String> length = new ArrayList<>();

    @Property
    ArrayList<String> width = new ArrayList<>();

    @Property
    ArrayList<String> height = new ArrayList<>();

    @Property
    String postage = "10";

    @Property
    String status = "";

    public Shipment(String senderName,
                    String senderAddress,
                    String senderZipCode,
                    String senderCity,
                    String recipientName,
                    String recipientAddress,
                    String recipientZipCode,
                    String recipientCity,
                    String[] packagingType,
                    String[] content,
                    String[] weight,
                    String[] length,
                    String[] width,
                    String[] height,
                    String postage,
                    String status){
        this.senderName = senderName;
        this.senderAddress = senderAddress;
        this.senderZipCode = senderZipCode;
        this.senderCity = senderCity;
        this.recipientName = recipientName;
        this.recipientAddress = recipientAddress;
        this.recipientZipCode = recipientZipCode;
        this.recipientCity = recipientCity;

        this.packagingType = new ArrayList<>(Arrays.asList(packagingType));
        this.content = new ArrayList<>(Arrays.asList(content));
        this.weight = new ArrayList<>(Arrays.asList(weight));
        this.width = new ArrayList<>(Arrays.asList(width));
        this.length = new ArrayList<>(Arrays.asList(length));
        this.height = new ArrayList<>(Arrays.asList(height));


        this.postage = postage;
        this.status = status;
    }

    public Shipment(){
    }

    public void setContent(ArrayList<String> content) {
        this.content = content;
    }

    public void setLength(ArrayList<String>  length) {
        this.length = length;
    }

    public void setPackagingType(ArrayList<String>  packagingType) {
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

    public void setHeight(ArrayList<String>  height) {
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

    public void setWeight(ArrayList<String>  weight) {
        this.weight = weight;
    }

    public void setWidth(ArrayList<String>  width) {
        this.width = width;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    public String toString(){
        return toJSONString();
    }

    public String toJSONString() {
        Gson gson = new Gson();
        return gson.toJson(this);
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
        ArrayList<String>  packagingMap = new Gson().fromJson(
                packagingTypeString, new TypeToken<ArrayList<String> >() {}.getType()
        );
        shipment.setPackagingType(packagingMap);

        String contentString = new JSONObject(json).get("content").toString();
        ArrayList<String>  contentMap = new Gson().fromJson(
                contentString, new TypeToken<ArrayList<String> >() {}.getType()
        );
        shipment.setContent(contentMap);

        String weightString = new JSONObject(json).get("weight").toString();
        ArrayList<String>  weightMap = new Gson().fromJson(
                weightString, new TypeToken<ArrayList<String> >() {}.getType()
        );
        shipment.setWeight(weightMap);

        String lengthString = new JSONObject(json).get("length").toString();
        ArrayList<String>  lengthMap = new Gson().fromJson(
                lengthString, new TypeToken<ArrayList<String> >() {}.getType()
        );
        shipment.setLength(lengthMap);

        String widthString = new JSONObject(json).get("width").toString();
        ArrayList<String>  widthMap = new Gson().fromJson(
                widthString, new TypeToken<ArrayList<String> >() {}.getType()
        );
        shipment.setWidth(widthMap);

        String heightString = new JSONObject(json).get("height").toString();
        ArrayList<String>  heightMap = new Gson().fromJson(
                heightString, new TypeToken<ArrayList<String> >() {}.getType()
        );
        shipment.setHeight(heightMap);

        return  shipment;
    }



}

