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
        

        //byte[] privateMetaObject = ("").getBytes(StandardCharsets.UTF_8);
        //when(stub.getPrivateData(collection, "MILK1_P")).thenReturn(privateMetaObject);
        //when(stub.getPrivateDataHash(collection, "MILK1_P")).thenReturn(("privateMetaObject").getBytes(StandardCharsets.UTF_8));
        
        //byte[]metaDef = ("").getBytes(StandardCharsets.UTF_8);
        //when(stub.getState("METADEF")).thenReturn(new byte[] {});

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
        public void readMetaDef() {           
            MetaDef returnedMetaDef = contract.META_readMetaDef(ctx);
            assertEquals(returnedMetaDef.getAttributeToDataTypeMap(), metaDef.getAttributeToDataTypeMap());
            assertEquals(returnedMetaDef.getProductNameToAttributesMap(), metaDef.getProductNameToAttributesMap());
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
            Exception thrown = assertThrows(RuntimeException.class, () -> {
                contract.META_addProductDefinition(ctx, "ham", attributes);
            });
            assertEquals(thrown.getMessage(), "The attribute Color is not defined");
        }        
    }
    
    @Nested
    class MetaObjectTests {

        @Test
        public void createObjectPublic() throws Exception {
            String[] attributes = {"Quality", "AmountInLiter"};
            String[] attrValues = {"good", "10"};
            contract.createObject(ctx, "MILK1", "CollectionOne", "milklot", attributes, attrValues);
            MetaObject mo = new MetaObject("CollectionOne", "milklot", attributes, attrValues, "2020-01-01T01:01:01Z", "Org1MSP");
            System.out.println(mo.toJSONString());
            String json = "{\"alarmFlag\":false,\"productName\":\"milklot\",\"receiver\":\"\",\"privateDataCollection\":\"CollectionOne\",\"predecessor\":[],\"successor\":[],\"actualOwner\":\"Org1MSP\",\"tsAndOwner\":{\"2020-01-01T01:01:01Z\":\"Org1MSP\"},\"attributes\":{\"Quality\":\"good\",\"AmountInLiter\":\"10\"}}";
            verify(stub).putState("MILK1", json.getBytes(UTF_8));
        }
    }

}