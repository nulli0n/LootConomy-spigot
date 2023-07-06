package su.nightexpress.lootconomy.data.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import su.nightexpress.lootconomy.LootConomyAPI;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.data.impl.SkillLimitData;
import su.nightexpress.lootconomy.skill.impl.Skill;
import su.nightexpress.lootconomy.skill.impl.Rank;

import java.lang.reflect.Type;
import java.util.Map;

public class SkillDataSerializer implements JsonDeserializer<SkillData>, JsonSerializer<SkillData> {

    @Override
    public SkillData deserialize(JsonElement json, Type type, JsonDeserializationContext contex) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();

        String jobId = object.get("job").getAsString();
        String rankId = object.get("rank").getAsString();
        int level = object.get("level").getAsInt();
        int xp = object.get("xp").getAsInt();

        Skill job = LootConomyAPI.getSkillById(jobId);
        if (job == null) return null;

        Rank rank = job.getRank(rankId);
        if (rank == null) rank = job.getRank(level);

		Map<String, Integer> perkLevels = contex.deserialize(object.get("perkLevels"), new TypeToken<Map<String, Integer>>(){}.getType());
        SkillLimitData limitData = contex.deserialize(object.get("dailyLimits"), new TypeToken<SkillLimitData>(){}.getType());

        return new SkillData(job, rank, level, xp, limitData, perkLevels);
    }

    @Override
    public JsonElement serialize(SkillData src, Type type, JsonSerializationContext contex) {

        JsonObject object = new JsonObject();
        object.addProperty("job", src.getSkill().getId());
        object.addProperty("rank", src.getRank().getId());
        object.addProperty("level", src.getLevel());
        object.addProperty("xp", src.getXP());
        object.add("perkLevels", contex.serialize(src.getPerkLevels()));
        object.add("dailyLimits", contex.serialize(src.getLimitData()));

        return object;
    }
}
