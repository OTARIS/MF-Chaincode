package de.metahlfabric;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType
class MetaAttribute<T> {

    @Property
    public final String name;
    @Property
    public T value;
    @Property
    public final int attributeVersion;

    MetaAttribute(String name, int version, T value) {
        this.name = name;
        this.attributeVersion = version;
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        return (obj instanceof MetaAttribute && ((MetaAttribute<?>) obj).name.equalsIgnoreCase(this.name)
                && ((MetaAttribute<?>) obj).value.equals(this.value)
                && ((MetaAttribute<?>) obj).attributeVersion == this.attributeVersion);
    }

}
