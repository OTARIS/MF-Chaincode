package org.example;

import java.util.HashMap;

import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

@DataType()
public class AcceptRule {

    @Property()
    String owner = "";

    @Property()
    String product = "";

    @Property()
    HashMap<String, String> rules = new HashMap<>();

    public AcceptRule(){

    }
    
}