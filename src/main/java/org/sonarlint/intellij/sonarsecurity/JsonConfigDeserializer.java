package org.sonarlint.intellij.sonarsecurity;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.Map;

public class JsonConfigDeserializer {
    public Map<String, RuleConfig> deserialize(String json) {

        Gson gson = new GsonBuilder().create();
        Type ruleConfigMapType = new TypeToken<Map<String, RuleConfig>>() {}.getType();
        return gson.fromJson(json, ruleConfigMapType);


    }

    public class RuleConfig {
        public MethodConfig[] sources;
        public MethodConfig[] sanitizers;
        public MethodConfig[] validators;
        public MethodConfig[] passthroughs;
        public MethodConfig[] sinks;
    }

    public class MethodConfig {
        String methodId;
        int[] args;
        boolean isMethodPrefix;
        Interval interval;
    }

    public class Interval {
        int fromIndex;
    }



}
