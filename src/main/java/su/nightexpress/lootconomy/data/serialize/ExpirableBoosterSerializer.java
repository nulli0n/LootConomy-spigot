package su.nightexpress.lootconomy.data.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;

import java.lang.reflect.Type;

public class ExpirableBoosterSerializer implements JsonSerializer<ExpirableBooster>, JsonDeserializer<ExpirableBooster> {

    @Override
    public ExpirableBooster deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        Multiplier multiplier = context.deserialize(object.get("multiplier"), new TypeToken<Multiplier>(){}.getType());
        long expireDate = object.get("expireDate").getAsLong();

        return new ExpirableBooster(multiplier, expireDate);
    }

    @Override
    public JsonElement serialize(ExpirableBooster booster, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.add("multiplier", context.serialize(booster.getMultiplier()));
        object.addProperty("expireDate", booster.getExpireDate());

        return object;
    }
}
