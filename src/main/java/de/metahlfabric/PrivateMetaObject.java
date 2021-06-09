package de.metahlfabric;

import com.google.gson.Gson;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * A PrivateMetaObject is linked to a {@link MetaObject} and stores the private information.
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
public class PrivateMetaObject {

    /**
     * The private attributes to store
     */
    @Property()
    ArrayList<MetaAttribute> attributes = new ArrayList<>();

    /**
     * Class constructor
     */
    public PrivateMetaObject() {
    }

    /**
     * @return the map of private attributes
     */
    public ArrayList<MetaAttribute> getAttributes() {
        return attributes;
    }

    /**
     * @param attrName  the name of the attribute to add
     * @param attrValue the value of the attribute to add
     */
    public void addAttribute(String attrName, int version, String attrValue) {
        this.deleteAttribute(attrName);
        attributes.add(new MetaAttribute(attrName, version, attrValue));
    }

    /**
     * @param attrName the name of the attribute to delete
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
}