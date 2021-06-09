package de.metahlfabric;

import com.google.gson.Gson;
import de.metahlfabric.MetaDef.AttributeDefinition;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * A MetaObject is a generic definition of the objects/assets stored in the blockchain.
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
     * The alarm flag of this object (is there a problem with this object?)
     */
    @Property()
    boolean alarmFlag = false;

    /**
     * The product name (which has to be defined in the MetaDef) of this object
     */
    @Property()
    String productName = "";

    @Property
    Integer productVersion = 1;

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
     * The list of keys of all predecessors of this object
     */
    @Property()
    ArrayList<Tuple<String, String>> predecessor = new ArrayList<>();

    /**
     * The list of keys of all successors of this object
     */
    @Property()
    ArrayList<Tuple<String, String>> successor = new ArrayList<>();

    /**
     * The list of timestamp and all owners of this object
     */
    @Property()
    ArrayList<Tuple<String, String>> tsAndOwner = new ArrayList<>();

    /**
     * The list of attributes defined in this object
     */
    @Property()
    ArrayList<MetaAttribute> attributes = new ArrayList<>();

    /**
     * Empty class constructor
     */
    public MetaObject() {
    }

    /**
     * Class constructor
     *
     * @param pdc                  the private data collection where to store the private data (empty if no private data necessary)
     * @param productName          the product name of this objects (defined in the MetaDef)
     * @param amount               the initial amount of this object
     * @param unit                 the unit definition of this object
     * @param attributeDefinitions the definitions of all attributes (defined in the MetaDef)
     * @param attrValues           the values of this object corresponding the attribute names
     * @param timeStamp            the time of the creation (auto generated)
     * @param owner                the initial owner of this object
     */
    public MetaObject(String pdc, String productName, Integer productVersion, double amount, String unit, List<AttributeDefinition> attributeDefinitions, String[] attrValues, String timeStamp, String owner) {
        this.productName = productName;
        this.productVersion = productVersion;
        if (!pdc.equals("") && !pdc.equals("null")) {
            this.privateDataCollection.add(pdc);
        }
        int i = 0;
        for (AttributeDefinition attributeDefinition : attributeDefinitions) {
            attributes.add(new MetaAttribute(attributeDefinition.getName(), attributeDefinition.getVersion(), attrValues[i]));
            i++;
        }
        tsAndOwner.add(new Tuple<>(timeStamp, owner));
        actualOwner = owner;
        this.unit = unit;
        this.amount = amount;
    }

    /**
     * @param key the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the key where to find the object
     */
    public String getKey() {
        return key;
    }

    /**
     * @param amount the amount to set
     */
    public void setAmount(double amount) {
        this.amount = amount;
    }

    /**
     * @return the actual amount
     */
    public double getAmount() {
        return amount;
    }

    /**
     * @param amount the amount to add
     */
    public void addAmount(double amount) {
        this.amount += amount;
    }

    /**
     * @param unit the unit to set
     */
    public void setUnit(String unit) {
        this.unit = unit;
    }

    /**
     * @return the unit definition
     */
    public String getUnit() {
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
    public void setAlarmFlag(boolean alarmFlag) {
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

    public Integer getProductVersion() {
        return this.productVersion;
    }

    public void setProductVersion(Integer version) {
        this.productVersion = version;
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
    public String getActualOwner() {
        return actualOwner;
    }

    /**
     * @param owner the actual owner to set
     */
    public void setActualOwner(String owner) {
        actualOwner = owner;
    }

    /**
     * @return the map of predecessors
     */
    public ArrayList<Tuple<String, String>> getPredecessor() {
        return predecessor;
    }

    /**
     * @param predecessor the predecessor to add
     * @param message     the message corresponding to the predecessor (How much was processed)
     */
    public void addPredecessor(String predecessor, String message) {
        this.predecessor.add(new Tuple<>(predecessor, message));
    }

    /**
     * @return the map of successors
     */
    public ArrayList<Tuple<String, String>> getSuccessor() {
        return successor;
    }

    /**
     * @param successor the successor to add
     * @param message   the message corresponding to the predecessor (How much was processed)
     */
    public void addSuccessor(String successor, String message) {
        this.successor.add(new Tuple<>(successor, message));
    }

    /**
     * @return the map of timestamp and owner
     */
    public ArrayList<Tuple<String, String>> getTsAndOwner() {
        return tsAndOwner;
    }

    /**
     * @param timeStamp the timestamp to add
     * @param owner     the owner corresponding to the timestamp
     */
    public void addTsAndOwner(String timeStamp, String owner) {
        tsAndOwner.add(new Tuple<>(timeStamp, owner));
    }

    /**
     * @return the map of attributes names and attribute values
     */
    public ArrayList<MetaAttribute> getAttributes() {
        return attributes;
    }

    /**
     * @param attrName  the attribute name to add
     * @param attrValue the attribute value to add
     */
    public void addAttribute(String attrName, int version, String attrValue) {
        this.deleteAttribute(attrName);
        attributes.add(new MetaAttribute(attrName, version, attrValue));
    }

    /**
     * @param attrName the attribute to delete
     */
    public void deleteAttribute(String attrName) {
        for (MetaAttribute attribute : this.attributes) {
            if (attribute.name.equalsIgnoreCase(attrName)) {
                attributes.remove(attribute);
                return;
            }
        }
    }

    /**
     * @return the object as a json string
     */
    public String toString() {
        return toJSONString();
    }

    /**
     * @return the object as a json string
     */
    public String toJSONString() {
        return new Gson().toJson(this);
    }

    /**
     * @return the json object
     */
    public JSONObject toJSON() {
        return new JSONObject(this.toJSONString());
    }

    @DataType
    class Tuple<X, Y> {

        @Property
        public final X x;
        @Property
        public final Y y;

        public Tuple(X x, Y y) {
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean equals(Object obj) {
            return (obj instanceof Tuple && ((Tuple) obj).x.equals(this.x) && ((Tuple) obj).y.equals(this.y));
        }
    }

}