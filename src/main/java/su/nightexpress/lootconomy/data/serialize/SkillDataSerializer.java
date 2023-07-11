package su.nightexpress.lootconomy.data.serialize;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import su.nightexpress.lootconomy.LootConomyAPI;
import su.nightexpress.lootconomy.data.impl.SkillData;
import su.nightexpress.lootconomy.data.impl.SkillLimitData;
import su.nightexpress.lootconomy.skill.impl.Rank;
import su.nightexpress.lootconomy.skill.impl.Skill;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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

        Set<Integer> obtainedLevelRewards;
        if (object.get("obtainedLevelRewards") == null) {
            obtainedLevelRewards = new HashSet<>();
        }
        else obtainedLevelRewards = contex.deserialize(object.get("obtainedLevelRewards"), new TypeToken<Set<Integer>>(){}.getType());

        return new SkillData(job, rank, level, xp, limitData, perkLevels, obtainedLevelRewards);
    }

    @Override
    public JsonElement serialize(SkillData data, Type type, JsonSerializationContext contex) {

        JsonObject object = new JsonObject();
        object.addProperty("job", data.getSkill().getId());
        object.addProperty("rank", data.getRank().getId());
        object.addProperty("level", data.getLevel());
        object.addProperty("xp", data.getXP());
        object.add("perkLevels", contex.serialize(data.getPerkLevels()));
        object.add("dailyLimits", contex.serialize(data.getLimitData()));
        object.add("obtainedLevelRewards", contex.serialize(data.getObtainedLevelRewards()));

        return object;
    }
}
