package org.sonarlint.intellij.sonarsecurity;

import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

public class JsonConfigDeserializerTest {



    @Test
    public void testDeserializeExample() throws IOException {
        String json = Files.readString(Paths.get("src/test/resources/sonarsecurityconfigexample.json"));
        Map<String, JsonConfigDeserializer.RuleConfig> config = new JsonConfigDeserializer().deserialize(new StringReader(json));

        assertThat(config).containsKey("S3649");
        assertThat(config.get("S3649").sanitizers).hasSize(1);
        assertThat(config.get("S3649").sanitizers[0].methodId).isEqualTo("my.package.StringUtils#stringReplace(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");
    }

}