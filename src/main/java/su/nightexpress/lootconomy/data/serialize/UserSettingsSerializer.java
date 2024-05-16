package su.nightexpress.lootconomy.data.serialize;

import com.google.gson.*;
import su.nightexpress.lootconomy.data.impl.UserSettings;

import java.lang.reflect.Type;

public class UserSettingsSerializer implements JsonSerializer<UserSettings>, JsonDeserializer<UserSettings> {

    @Override
    public UserSettings deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();

        boolean soundEnabled = object.get("pickupSound").getAsBoolean();

        return new UserSettings(soundEnabled);
    }

    @Override
    public JsonElement serialize(UserSettings settings, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();

        object.addProperty("pickupSound", settings.isPickupSound());

        return object;
    }
}
