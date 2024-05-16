package su.nightexpress.lootconomy.data.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import su.nightexpress.lootconomy.booster.Multiplier;

import java.lang.reflect.Type;
import java.util.Map;

public class BoosterMultiplierSerializer implements JsonDeserializer<Multiplier>, JsonSerializer<Multiplier> {

    @Override
    public Multiplier deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        Map<String, Double> currencyMod = context.deserialize(object.get("currencyMultiplier"), new TypeToken<Map<String, Double>>(){}.getType());

        return new Multiplier(currencyMod);
    }

    @Override
    public JsonElement serialize(Multiplier multiplier, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.add("currencyMultiplier", context.serialize(multiplier.getCurrencyMap()));

        return object;
    }
}
