package de.metahlfabric;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import javax.management.AttributeNotFoundException;
import java.util.ArrayList;
import java.util.List;

/**
 * The MetaDef defines the attributes and assets available in this channel.
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
public class MetaDef {

    private enum ChangeType {ADD, DELETE}
    public enum AttributeDataType {Integer, Float, String, IntegerArray, FloatArray, StringArray}

    @Property
    ArrayList<AttributeDefinition> attributeDefinitions;

    @Property
    ArrayList<AssetDefinition> assetDefinitions;

    @Property
    ArrayList<String> units;

    /**
     * Class constructor
     */
    public MetaDef() {
        attributeDefinitions = new ArrayList<>();
        assetDefinitions = new ArrayList<>();
        units = new ArrayList<>();
    }

    /**
     * @return the list of defined units
     */
    public ArrayList<String> getUnitList() {
        return this.units;
    }

    /**
     * @param unit the unit definition to add
     */
    public void addUnitToUnitList(String unit) {
        this.units.add(unit);
    }

    /**
     * @return the hash map of defined attributes
     */
    public List<AttributeDefinition> getAttributeList() {
        return this.attributeDefinitions;
    }

    /**
     * @param attribute the attribute to add
     * @param dataType  the data type of the attribute to add
     */
    public void addAttributeDefinition(String attribute, String dataType) {
        AttributeDataType mDataType;
        switch (dataType) {
            case "Integer":
            case "int":
            case "Long":
            case "long":
                mDataType = AttributeDataType.Integer;
                break;
            case "Float":
            case "float":
            case "Double":
            case "double":
                mDataType = AttributeDataType.Float;
                break;
            case "Array":
            case "StringArray":
            case "ArrayOfString":
                mDataType = AttributeDataType.StringArray;
                break;
            case "intArray":
            case "IntegerArray":
            case "ArrayOfInt":
            case "ArrayOfInteger":
            case "longArray":
            case "LongArray":
            case "ArrayOfLong":
                mDataType = AttributeDataType.IntegerArray;
                break;
            case "floatArray":
            case "FloatArray":
            case "ArrayOfFloat":
            case "doubleArray":
            case "DoubleArray":
            case "ArrayOfDouble":
                mDataType = AttributeDataType.FloatArray;
                break;
            default:
                mDataType = AttributeDataType.String;
        }
        for (AttributeDefinition attributeDefinition : this.attributeDefinitions)
            if (attributeDefinition.getName().equalsIgnoreCase(attribute)) {
                attributeDefinition.setDataType(mDataType);
                for (AssetDefinition assetDefinition : this.assetDefinitions) {
                    for (AttributeDefinition oldAttributeDefinition : assetDefinition.getAttributes()) {
                        if (oldAttributeDefinition.getName().equalsIgnoreCase(attribute)) {
                            try {
                                assetDefinition.removeAttribute(oldAttributeDefinition);
                                assetDefinition.addAttribute(attributeDefinition);
                            } catch (AttributeNotFoundException e) {
                                // no harm was done
                            }
                        }
                    }
                }
                return;
            }
        this.attributeDefinitions.add(new AttributeDefinition(attribute, mDataType));
    }

    /**
     * @return the HashMap of defined products
     */
    public List<AssetDefinition> getAssetDefinitions() {
        return this.assetDefinitions;
    }

    /**
     * @param assetName get attributes of this product
     * @return the attributes of the specified product
     */
    public List<AttributeDefinition> getAttributesByAssetNameAndVersion(String assetName, Integer version) {
        for (AssetDefinition assetDefinition : this.assetDefinitions)
            if (assetDefinition.getName().equalsIgnoreCase(assetName))
                return assetDefinition.getAttributes(version);
        return null;
    }

    /**
     * @param assetName  the product to add
     * @param attributes the attributes to add
     */
    public void addAssetDefinition(String assetName, ArrayList<String> attributeNames,
                                   ArrayList<AttributeDefinition> attributes) {
        for (AssetDefinition assetDefinition : this.assetDefinitions) {
            if (assetDefinition.getName().equalsIgnoreCase(assetName)) {
                // Asset already exists!
                List<AttributeDefinition> existingAttributes = new ArrayList<>(assetDefinition.getAttributes());
                for (AttributeDefinition existingAttribute : existingAttributes) {
                    if (!attributeNames.contains(existingAttribute.getName())) {
                        try {
                            assetDefinition.removeAttribute(existingAttribute);
                        } catch (AttributeNotFoundException e) {
                            // no harm
                        }
                    } else {
                        attributeNames.remove(existingAttribute.getName());
                    }
                }
                for (AttributeDefinition attributeDefinition : attributes) {
                    if (attributeNames.contains(attributeDefinition.getName())) {
                        assetDefinition.addAttribute(attributeDefinition);
                    }
                }
                return;
            }
        }

        this.assetDefinitions.add(new AssetDefinition(assetName, attributes));
    }

    public boolean deleteAssetDefinition(String assetName) {
        return this.assetDefinitions.removeIf(assetDefinition -> assetDefinition.getName().equalsIgnoreCase(assetName));
    }

    /**
     * @param productName the product to check
     * @return true if the product exists
     */
    public boolean assetNameExists(String productName) {
        for (AssetDefinition assetDefinition : this.assetDefinitions)
            if (assetDefinition.getName().equalsIgnoreCase(productName))
                return true;
        return false;
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
    public JsonObject toJSON() {
        return new Gson().fromJson(this.toJSONString(), JsonObject.class);
    }

    @DataType
    class AttributeDefinition {

        @Property
        private final String name;
        @Property
        private final ArrayList<AttributeDataType> dataTypeHistory;
        @Property
        private AttributeDataType dataType;
        @Property
        private Integer version;

        public AttributeDefinition(String name, AttributeDataType dataType) {
            this.dataTypeHistory = new ArrayList<>();
            this.name = name;
            this.dataType = dataType;
            this.version = 1;
        }

        public void setDataType(AttributeDataType dataType) {
            this.dataTypeHistory.add(this.dataType);
            this.dataType = dataType;
            this.version++;
        }

        public Integer getVersion() {
            return this.version;
        }

        public String getName() {
            return this.name;
        }

        public AttributeDataType getDataType() {
            return this.dataType;
        }

        public AttributeDataType getDataType(int version) {
            if (version == this.version)
                return dataType;
            else if (version > this.version || version < 1)
                return null;
            else
                return this.dataTypeHistory.get(version - 1);
        }

        @Override
        public boolean equals(Object obj) {
            return obj instanceof AttributeDefinition && ((AttributeDefinition) obj).getName().equalsIgnoreCase(this.name)
                    && ((AttributeDefinition) obj).getVersion().equals(this.version);
        }
    }

    @DataType
    class AssetDefinition {
        @Property
        private final String name;
        @Property
        private final ArrayList<AttributeDefinition> attributes;
        @Property
        private Integer version;
        @Property
        private final ArrayList<AttributeChange> changeHistory;

        AssetDefinition(String name, ArrayList<AttributeDefinition> attributes) {
            this.name = name;
            this.attributes = attributes;
            this.version = 1;
            this.changeHistory = new ArrayList<>();
        }

        public String getName() {
            return this.name;
        }

        public List<AttributeDefinition> getAttributes() {
            return this.attributes;
        }

        public List<AttributeDefinition> getAttributes(int version) {
            if (version < 1)
                return null;
            ArrayList<AttributeDefinition> attributeDefinitionsOfVersion = new ArrayList<>(this.attributes);

            for (int i = this.version - 2; i >= version - 1; i--) {
                AttributeChange attributeChange = this.changeHistory.get(i);
                if (attributeChange.type.equals(ChangeType.ADD)) {
                    attributeDefinitionsOfVersion.removeIf(attributeDefinitionOfVersion
                            -> attributeDefinitionOfVersion.getName().equalsIgnoreCase(attributeChange.attribute.getName())
                            && attributeDefinitionOfVersion.getVersion().equals(attributeChange.attribute.getVersion()));
                } else
                    attributeDefinitionsOfVersion.add(attributeChange.attribute);
            }

            return attributeDefinitionsOfVersion;
        }

        void addAttribute(AttributeDefinition attribute) throws NullPointerException {
            if (attribute == null)
                throw new NullPointerException("Attempt to add an attribute but the parameter given was Null!");
            for (AttributeDefinition attributeDefinition : this.attributes)
                if (attributeDefinition.getName().equalsIgnoreCase(attribute.getName())
                        && attributeDefinition.getVersion().equals(attribute.getVersion()))
                    return;
            this.attributes.add(attribute);
            this.changeHistory.add(new AttributeChange(ChangeType.ADD, attribute));
            this.version++;
        }

        void removeAttribute(AttributeDefinition attribute) throws NullPointerException,
                AttributeNotFoundException {
            if (attribute == null)
                throw new NullPointerException("Attempt to remove an attribute but the parameter given was Null!");
            if (!this.attributes.removeIf(attributeDefinition -> attributeDefinition.getName().equalsIgnoreCase(attribute.getName())
                    && attributeDefinition.getVersion().equals(attribute.getVersion())))
                throw new AttributeNotFoundException("Attempt to remove an attribute which is not in the attribute list!");
            this.changeHistory.add(new AttributeChange(ChangeType.DELETE, attribute));
            this.version++;
        }

        Integer getVersion() {
            return this.version;
        }

        @DataType
        private class AttributeChange {
            @Property
            final ChangeType type;
            @Property
            final AttributeDefinition attribute;

            AttributeChange(ChangeType type, AttributeDefinition attribute) {
                this.type = type;
                this.attribute = attribute;
            }
        }

    }

}