package de.metahlfabric;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
class MetaAttribute {

    @Property
    public final String name;
    @Property
    public String value;
    @Property
    public final int attributeVersion;

    MetaAttribute(String name, int version, String value) {
        this.name = name;
        this.attributeVersion = version;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MetaAttribute && ((MetaAttribute) obj).name.equalsIgnoreCase(this.name)
                && ((MetaAttribute) obj).value.equalsIgnoreCase(this.value)
                && ((MetaAttribute) obj).attributeVersion == this.attributeVersion);
    }

}
