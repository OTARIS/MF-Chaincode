package de.metahlfabric;

import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public final class MetaChainTest {

    Context ctx;
    ChaincodeStub stub;
    MFContract contract;

    String collection = "CollectionOne";

    @BeforeEach
    void BeforeEach() {
        ctx = mock(Context.class);
        stub = mock(ChaincodeStub.class);
        when(ctx.getStub()).thenReturn(stub);
        contract = new MFContract();
    }

    @Nested
    class MetaDefTests {

        @Test
        public void createSampleData() {
            String result = contract.META_addUnit(ctx, "Liter");
            System.out.println(result);
            assertTrue(result.contains("200"));
            result = contract.META_addUnit(ctx, "Pounds");
            System.out.println(result);
            assertTrue(result.contains("200"));
        }

        @Test
        public void addAttributeDefinition() {
            String result = contract.META_addAttributeDefinition(ctx, "Color", "String");
            System.out.println(result);
            assertTrue(result.contains("200"));
            result = contract.META_addAttributeDefinition(ctx, "Quality", "String");
            System.out.println(result);
            assertTrue(result.contains("200"));
            result = contract.META_addAttributeDefinition(ctx, "Quality", "Integer");
            System.out.println(result);
            assertTrue(result.contains("200"));
        }

        @Test
        public void addProductDefinition() {
            String[] attributes = {"Quality"};
            String result = contract.META_addProductDefinition(ctx, "ham", attributes);
            System.out.println(result);
            assertTrue(result.contains("200"));
            result = contract.META_addProductDefinition(ctx, "milklot", attributes);
            System.out.println(result);
            assertTrue(result.contains("200"));

        }

        @Test
        public void addProductDefinitionWithWrongAttribute() {
            String[] attributes = {"AmountInLiter"};
            String result = contract.META_addProductDefinition(ctx, "jam", attributes);
            assertFalse(result.contains("200"));
        }
    }

    @Nested
    class CreateObjectTests {

        @Test
        public void createObjectPublic() {
            String[] attributes = {};
            String[] attrValues = {};
            String result = contract.createObject(ctx, "MILK1", collection, "milklot", "4", "Liter", attributes, attrValues);
            System.out.println(result);
            assertTrue(result.contains("200"));
        }

    }

    @Nested
    class TrackObjectTests {
        @Test
        @DisplayName("should return a list of transactions for an existing valid object")
        public void getAssetTransactionHistoryWithValidId() {
            String[] attributes = {};
            String[] attrValues = {};
            String productCreation = contract.createObject(ctx, "AVALIDID1", collection, "milklot", "4", "Liter", attributes, attrValues);
            assertTrue(productCreation.contains("200"));
            String id = "AVALIDID1";
            String result = contract.getAssetHistoryList(ctx, id);
            assertTrue(result.contains("200"));
        }

        @Test
        @DisplayName("should return an exception for an invalid object")
        public void getAssetTransactionHistoryWithInvalidId() {
            String id = "SOMEINVALIDSTUFF1";
            String result = contract.getAssetHistoryList(ctx,id);
            System.setOut(System.out);
            System.out.println(result);
            assertTrue(result.contains("400"));
        }
    }

}