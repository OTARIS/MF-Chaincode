package org.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.hyperledger.fabric.contract.ClientIdentity;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

public final class MetaChainTest {
    
    Context ctx;
    ChaincodeStub stub;
    NutriSafeContract contract;
    MetaDef metaDef;
    ClientIdentity clientIdentity;

    String collection = "CollectionOne";

    @BeforeEach
    void BeforeEach() {
        ctx = mock(Context.class);
        stub = mock(ChaincodeStub.class);
        clientIdentity = mock(ClientIdentity.class);
        when(ctx.getStub()).thenReturn(stub);
        when(ctx.getClientIdentity()).thenReturn(clientIdentity);

        contract = new NutriSafeContract();

        metaDef = new MetaDef();
        metaDef.createSampleData();
        when(stub.getState("METADEF")).thenReturn(metaDef.toJSONString().getBytes(StandardCharsets.UTF_8));
        Instant instant = Instant.parse("2020-01-01T01:01:01Z");
        when(stub.getTxTimestamp()).thenReturn(instant);
        when(ctx.getClientIdentity().getMSPID()).thenReturn("Org1MSP");
        


    }

    @Nested
    class MetaDefTests {
        @Test
        public void createSampleData(){
            contract.META_createSampleData(ctx);          
            String json = "{\"productNameToAttributesMap\":{\"milklot\":[\"Quality\",\"AmountInLiter\"]},\"attributeToDataTypeMap\":{\"AmountInLiter\":\"Integer\",\"Quality\":\"String\"}}";
            verify(stub).putState("METADEF", json.getBytes(UTF_8));
        }
        
        @Test
        public void addAttributeDefinition(){
            String json = "{\"productNameToAttributesMap\":{\"milklot\":[\"Quality\",\"AmountInLiter\"]},\"attributeToDataTypeMap\":{\"AmountInLiter\":\"Integer\",\"Quality\":\"String\",\"Color\":\"String\"}}";
            contract.META_addAttributeDefinition(ctx, "Color", "String");
            verify(stub).putState("METADEF", json.getBytes(UTF_8));
        }

        @Test
        public void addProductDefinition(){
            String json = "{\"productNameToAttributesMap\":{\"ham\":[\"Quality\",\"AmountInLiter\"],\"milklot\":[\"Quality\",\"AmountInLiter\"]},\"attributeToDataTypeMap\":{\"AmountInLiter\":\"Integer\",\"Quality\":\"String\"}}";    
            String[] attributes = {"Quality", "AmountInLiter"};
            contract.META_addProductDefinition(ctx, "ham", attributes);
            verify(stub).putState("METADEF", json.getBytes(UTF_8));
        }

        @Test
        public void addProductDefinitionWithWrongAttribute(){
            String[] attributes = {"Color", "AmountInLiter"};
            String result = contract.META_addProductDefinition(ctx, "ham", attributes);
            String json = "{\"response\":\"The attribute Color is not defined\",\"status\":\"400\"}";
            assertEquals(json, result);
        }        
    }
    
    @Nested
    class CreateObjectTests {

        @Test
        public void createObjectPublic() throws Exception {
            String[] attributes = {"Quality", "AmountInLiter"};
            String[] attrValues = {"good", "10"};
            String result = contract.createObject(ctx, "MILK1", "CollectionOne", "milklot", attributes, attrValues);
            assertTrue(result.contains("200"));
        }

        @Test
        public void createObjectWithUndefinedProduct() throws Exception {
            String[] attributes = {"Quality", "AmountInLiter"};
            String[] attrValues = {"good", "10"};
            String result = contract.createObject(ctx, "MILK1", "CollectionOne", "ham", attributes, attrValues);
            String json = "{\"response\":\"The product name ham is not defined\",\"status\":\"400\"}";
            assertEquals(json, result);
        }
        
        @Test
        public void createObjectWithUndefinedAttribute() throws Exception {
            String[] attributes = {"Color", "AmountInLiter"};
            String[] attrValues = {"good", "10"};
            String result = contract.createObject(ctx, "MILK1", "CollectionOne", "milklot", attributes, attrValues);
            String json = "{\"response\":\"The attribute Color is not defined\",\"status\":\"400\"}";
            assertEquals(json, result);
        }

        @Test
        public void createObjectWithStringAsIntegerAttribute() throws Exception {
            String[] attributes = {"Quality", "AmountInLiter"};
            String[] attrValues = {"good", "knlknkl"};
            String result = contract.createObject(ctx, "MILK1", "CollectionOne", "milklot", attributes, attrValues);
            String json = "{\"response\":\"The attribute AmountInLiter is not an Integer\",\"status\":\"400\"}";
            assertEquals(json, result);
        }

        @Test
        public void createObjectWithUndefinedPrivateAttribute() throws Exception {
            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("Color", "150".getBytes(StandardCharsets.UTF_8));
            when(stub.getTransient()).thenReturn(transientMap);
            String[] attributes = {"Quality"};
            String[] attrValues = {"good"};
            String result = contract.createObject(ctx, "MILK1", "CollectionOne", "milklot", attributes, attrValues);
            String json = "{\"response\":\"The attribute Color is not defined\",\"status\":\"400\"}";
            assertEquals(json, result);
        }

        @Test
        public void createObjectWithStringAsIntegerAttributePrivate() throws Exception {
            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("AmountInLiter", "150L".getBytes(StandardCharsets.UTF_8));
            when(stub.getTransient()).thenReturn(transientMap);
            String[] attributes = {"Quality"};
            String[] attrValues = {"good"};
            String result = contract.createObject(ctx, "MILK1", "CollectionOne", "milklot", attributes, attrValues);
            String json = "{\"response\":\"The attribute AmountInLiter is not an Integer\",\"status\":\"400\"}";
            assertEquals(json, result);
        }

    }

    @Nested
    class AcceptRuleTests {

        @Test
        public void addRuleNameAndConditionNew() throws Exception {
            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("Quality", "eqBio".getBytes(StandardCharsets.UTF_8));
            when(stub.getTransient()).thenReturn(transientMap);
            String response = contract.addRuleNameAndCondition(ctx, "CollectionOne", "milklot");
            assertTrue(response.contains("200"));
        }

        @Test
        public void addRuleNameAndConditionExist() throws Exception {
            AcceptRule acr = new AcceptRule();
            acr.addEntryToProductToAttributeAndRule("ham", "Quality", "eqBio");
            byte[] acrBytes = acr.toJSONString().getBytes(StandardCharsets.UTF_8);
            when(stub.getPrivateData(collection, "Org1MSP_ACR")).thenReturn(acrBytes);
            when(stub.getPrivateDataHash(collection, "Org1MSP_ACR")).thenReturn(("privateMetaObject").getBytes(StandardCharsets.UTF_8));
            Map<String, byte[]> transientMap = new HashMap<>();
            transientMap.put("Quality", "eqBio".getBytes(StandardCharsets.UTF_8));
            when(stub.getTransient()).thenReturn(transientMap);
            String response = contract.addRuleNameAndCondition(ctx, "CollectionOne", "milklot");
            assertTrue(response.contains("200"));
            assertTrue(response.contains("ham"));
        }

        @Test
        public void deleteRuleForProduct() throws Exception {
            AcceptRule acr = new AcceptRule();
            acr.addEntryToProductToAttributeAndRule("milklot", "Quality", "eqBio");
            acr.addEntryToProductToAttributeAndRule("ham", "AmountInLiter", "eq10");
            byte[] acrBytes = acr.toJSONString().getBytes(StandardCharsets.UTF_8);
            when(stub.getPrivateData(collection, "Org1MSP_ACR")).thenReturn(acrBytes);
            when(stub.getPrivateDataHash(collection, "Org1MSP_ACR")).thenReturn(("privateMetaObject").getBytes(StandardCharsets.UTF_8));
            String response = contract.deleteRuleForProduct(ctx, "CollectionOne", "milklot");
            assertTrue(response.contains("ham"));
            assertFalse(response.contains("milklot"));
        }
    }

    @Nested
    class setReceiverTests {

        @Test
        public void setReceiverWithoutACR() throws Exception {
            String[] attrNames = {"Quality"};
            String[] attrValues = {"good"};
            MetaObject milk1 = new MetaObject("", "milklot", attrNames, attrValues, "01.01.01", "Org1MSP");
            when(stub.getState("MILK1")).thenReturn(milk1.toJSONString().getBytes(StandardCharsets.UTF_8));
            String result = contract.setReceiver(ctx, "MILK1", "Org2MSP", "CollectionOne");
            assertTrue(result.contains("200"));
            assertTrue(result.contains("Org2MSP"));
        }

        @Test
        public void setReceiverWithWrongOwner() throws Exception {
            String[] attrNames = {"Quality"};
            String[] attrValues = {"good"};
            MetaObject milk1 = new MetaObject("", "milklot", attrNames, attrValues, "01.01.01", "Org2MSP");
            when(stub.getState("MILK1")).thenReturn(milk1.toJSONString().getBytes(StandardCharsets.UTF_8));
            String result = contract.setReceiver(ctx, "MILK1", "Org2MSP", "CollectionOne");
            String json = "{\"response\":\"You (Org1MSP) are not the actual owner\",\"status\":\"400\"}";
            assertEquals(json, result);
        }

        @Test
        public void setReceiverWithACR() throws Exception {
            String[] attrNames = {"Quality"};
            String[] attrValues = {"Bio"};
            MetaObject milk1 = new MetaObject("", "milklot", attrNames, attrValues, "01.01.01", "Org1MSP");
            when(stub.getState("MILK1")).thenReturn(milk1.toJSONString().getBytes(StandardCharsets.UTF_8));
            AcceptRule acr = new AcceptRule();
            acr.addEntryToProductToAttributeAndRule("milklot", "Quality", "eqBio");
            acr.addEntryToProductToAttributeAndRule("ham", "AmountInLiter", "eq10");
            byte[] acrBytes = acr.toJSONString().getBytes(StandardCharsets.UTF_8);
            when(stub.getPrivateData(collection, "Org2MSP_ACR")).thenReturn(acrBytes);
            when(stub.getPrivateDataHash(collection, "Org2MSP_ACR")).thenReturn(("privateMetaObject").getBytes(StandardCharsets.UTF_8));
            String result = contract.setReceiver(ctx, "MILK1", "Org2MSP", collection);
            assertTrue(result.contains("200"));
            assertTrue(result.contains("Org2MSP"));
        }

        @Test
        public void setReceiverWithACRAndPrivateAttributes() throws Exception {
            String[] attrNames = {"Quality"};
            String[] attrValues = {"Bio"};
            MetaObject milk1 = new MetaObject(collection, "milklot", attrNames, attrValues, "01.01.01", "Org1MSP");
            when(stub.getState("MILK1")).thenReturn(milk1.toJSONString().getBytes(StandardCharsets.UTF_8));
            
            PrivateMetaObject pmo = new PrivateMetaObject();
            pmo.addAttribute("AmountInLiter", "10");
            byte[] pmoBytes = pmo.toJSONString().getBytes(StandardCharsets.UTF_8);
            when(stub.getPrivateData(collection, "MILK1_P")).thenReturn(pmoBytes);
            
            AcceptRule acr = new AcceptRule();
            acr.addEntryToProductToAttributeAndRule("milklot", "Quality", "eqBio");
            acr.addEntryToProductToAttributeAndRule("milklot", "AmountInLiter", "gt9");
            byte[] acrBytes = acr.toJSONString().getBytes(StandardCharsets.UTF_8);
            when(stub.getPrivateData(collection, "Org2MSP_ACR")).thenReturn(acrBytes);
            when(stub.getPrivateDataHash(collection, "Org2MSP_ACR")).thenReturn(("privateMetaObject").getBytes(StandardCharsets.UTF_8));
            String result = contract.setReceiver(ctx, "MILK1", "Org2MSP", collection);
            assertTrue(result.contains("200"));
            assertTrue(result.contains("Org2MSP"));
        }

        @Test
        public void setReceiverWithWrongEqCondition() throws Exception {
            String[] attrNames = {"Quality"};
            String[] attrValues = {"good"};
            MetaObject milk1 = new MetaObject("", "milklot", attrNames, attrValues, "01.01.01", "Org1MSP");
            when(stub.getState("MILK1")).thenReturn(milk1.toJSONString().getBytes(StandardCharsets.UTF_8));
            AcceptRule acr = new AcceptRule();
            acr.addEntryToProductToAttributeAndRule("milklot", "Quality", "eqBio");
            byte[] acrBytes = acr.toJSONString().getBytes(StandardCharsets.UTF_8);
            when(stub.getPrivateData(collection, "Org2MSP_ACR")).thenReturn(acrBytes);
            when(stub.getPrivateDataHash(collection, "Org2MSP_ACR")).thenReturn(("privateMetaObject").getBytes(StandardCharsets.UTF_8));
            String result = contract.setReceiver(ctx, "MILK1", "Org2MSP", collection);
            String json = "{\"response\":\"The attribute Quality with the value good does not match the condition Bio\",\"status\":\"400\"}";
            assertEquals(json, result);
        }

        @Test
        public void setReceiverWithWrongLtCondition() throws Exception {
            String[] attrNames = {"AmountInLiter"};
            String[] attrValues = {"10"};
            MetaObject milk1 = new MetaObject("", "milklot", attrNames, attrValues, "01.01.01", "Org1MSP");
            when(stub.getState("MILK1")).thenReturn(milk1.toJSONString().getBytes(StandardCharsets.UTF_8));
            AcceptRule acr = new AcceptRule();
            acr.addEntryToProductToAttributeAndRule("milklot", "AmountInLiter", "lt5");
            byte[] acrBytes = acr.toJSONString().getBytes(StandardCharsets.UTF_8);
            when(stub.getPrivateData(collection, "Org2MSP_ACR")).thenReturn(acrBytes);
            when(stub.getPrivateDataHash(collection, "Org2MSP_ACR")).thenReturn(("privateMetaObject").getBytes(StandardCharsets.UTF_8));
            String result = contract.setReceiver(ctx, "MILK1", "Org2MSP", collection);
            String json = "{\"response\":\"The attribute AmountInLiter with the value 10 is not lower than 5\",\"status\":\"400\"}";
            assertEquals(json, result);
        }

        @Test
        public void setReceiverWithWrongGtCondition() throws Exception {
            String[] attrNames = {"AmountInLiter"};
            String[] attrValues = {"10"};
            MetaObject milk1 = new MetaObject("", "milklot", attrNames, attrValues, "01.01.01", "Org1MSP");
            when(stub.getState("MILK1")).thenReturn(milk1.toJSONString().getBytes(StandardCharsets.UTF_8));
            AcceptRule acr = new AcceptRule();
            acr.addEntryToProductToAttributeAndRule("milklot", "AmountInLiter", "gt15");
            byte[] acrBytes = acr.toJSONString().getBytes(StandardCharsets.UTF_8);
            when(stub.getPrivateData(collection, "Org2MSP_ACR")).thenReturn(acrBytes);
            when(stub.getPrivateDataHash(collection, "Org2MSP_ACR")).thenReturn(("privateMetaObject").getBytes(StandardCharsets.UTF_8));
            String result = contract.setReceiver(ctx, "MILK1", "Org2MSP", collection);
            String json = "{\"response\":\"The attribute AmountInLiter with the value 10 is not greater than 15\",\"status\":\"400\"}";
            assertEquals(json, result);
        }
    }

}