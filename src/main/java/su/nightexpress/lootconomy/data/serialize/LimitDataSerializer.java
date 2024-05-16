package su.nightexpress.lootconomy.data.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import su.nightexpress.lootconomy.data.impl.LootLimitData;

import java.lang.reflect.Type;
import java.util.Map;

public class LimitDataSerializer implements JsonSerializer<LootLimitData>, JsonDeserializer<LootLimitData> {

    @Override
    public LootLimitData deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        Map<String, Double> currencyEarned = context.deserialize(object.get("currencyEarned"), new TypeToken<Map<String, Double>>(){}.getType());
        long expireDate = object.get("expireDate").getAsLong();

        return new LootLimitData(currencyEarned, expireDate);
    }

    @Override
    public JsonElement serialize(LootLimitData data, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.add("currencyEarned", context.serialize(data.getCurrencyEarned()));
        object.addProperty("expireDate", data.getExpireDate());

        return object;
    }
}
