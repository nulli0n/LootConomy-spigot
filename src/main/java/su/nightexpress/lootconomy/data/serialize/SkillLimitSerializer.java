package su.nightexpress.lootconomy.data.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import su.nightexpress.lootconomy.data.impl.SkillLimitData;

import java.lang.reflect.Type;
import java.util.Map;

public class SkillLimitSerializer implements JsonSerializer<SkillLimitData>, JsonDeserializer<SkillLimitData> {

    @Override
    public SkillLimitData deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        String job = object.get("jobId").getAsString();
        Map<String, Double> currencyEarned = context.deserialize(object.get("currencyEarned"), new TypeToken<Map<String, Double>>(){}.getType());
        int xpEarned = object.get("xpEarned").getAsInt();
        long since = object.get("since").getAsLong();

        return new SkillLimitData(job, currencyEarned, xpEarned, since);
    }

    @Override
    public JsonElement serialize(SkillLimitData data, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.addProperty("jobId", data.getJobId());
        object.add("currencyEarned", context.serialize(data.getCurrencyEarned()));
        object.addProperty("xpEarned", data.getXPEarned());
        object.addProperty("since", data.getSince());

        return object;
    }
}
