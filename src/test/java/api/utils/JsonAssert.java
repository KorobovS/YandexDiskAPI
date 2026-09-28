package api.utils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import io.restassured.response.Response;
import org.junit.jupiter.api.Assertions;

import java.util.Set;

public abstract class JsonAssert {

    public static void checkSchema(String path, Response response) {

        ObjectMapper objectMapper = new ObjectMapper();

        try {
            JsonNode jsonNode = objectMapper.readTree(response.body().asString());

            JsonSchemaFactory factory = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7);
            JsonSchema schema = factory.getSchema(
                    JsonAssert.class.getClassLoader().getResourceAsStream(path)
            );

            Set<ValidationMessage> errors = schema.validate(jsonNode);

            Assertions.assertTrue(errors.isEmpty(), "Ошибка при валидации schema: " + errors);

        } catch (Exception e) {
            response.prettyPrint();
            throw new RuntimeException("Ошибка при валидации схемы", e);
        }
    }
}
