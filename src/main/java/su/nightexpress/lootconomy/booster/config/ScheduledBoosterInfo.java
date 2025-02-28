package su.nightexpress.lootconomy.booster.config;

import org.jetbrains.annotations.NotNull;
import su.nightexpress.lootconomy.booster.Multiplier;
import su.nightexpress.lootconomy.booster.impl.ExpirableBooster;
import su.nightexpress.nightcore.config.FileConfig;
import su.nightexpress.nightcore.util.StringUtil;
import su.nightexpress.nightcore.util.TimeUtil;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ScheduledBoosterInfo extends BoosterInfo {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    private final Map<DayOfWeek, Set<LocalTime>> startTimes;
    private final int duration;

    public ScheduledBoosterInfo(@NotNull Multiplier multiplier,
                                @NotNull Map<DayOfWeek, Set<LocalTime>> startTimes,
                                int duration) {
        super(multiplier);
        this.startTimes = startTimes;
        this.duration = duration;
    }

    @NotNull
    public static ScheduledBoosterInfo read(@NotNull FileConfig config, @NotNull String path) {
        Multiplier multiplier = Multiplier.read(config, path);

        Map<DayOfWeek, Set<LocalTime>> startTimes = new HashMap<>();
        for (String dayName : config.getSection(path + ".Start_Times")) {
            DayOfWeek day = StringUtil.getEnum(dayName, DayOfWeek.class).orElse(null);
            if (day == null) continue;

            Set<LocalTime> times = new HashSet<>();
            config.getStringSet(path + ".Start_Times." + dayName).forEach(raw -> {
                try {
                    times.add(LocalTime.parse(raw, TIME_FORMATTER).truncatedTo(ChronoUnit.MINUTES));
                }
                catch (DateTimeParseException ignored) {}
            });

            startTimes.put(day, times);
        }

        int duration = config.getInt(path + ".Duration");

        return new ScheduledBoosterInfo(multiplier, startTimes, duration);
    }

    public void write(@NotNull FileConfig config, @NotNull String path) {
        this.startTimes.forEach((day, times) -> {
            config.set(path + ".Start_Times." + day.name(), times.stream().map(time -> time.format(TIME_FORMATTER)).toList());
        });
        config.set(path + ".Duration", this.getDuration());
        this.getMultiplier().write(config, path);
    }

    @Override
    @NotNull
    public ExpirableBooster createBooster() {
        return new ExpirableBooster(this);
    }

    /*@Nullable
    public LocalDateTime getClosest() {
        if (this.startTimes.isEmpty()) return null;

        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);

        int dayCounter = 0;
        while (dayCounter < 8) {
            LocalDateTime adjusted = LocalDateTime.now().plusDays(dayCounter);
            DayOfWeek day = adjusted.getDayOfWeek();

            Set<LocalTime> times = this.startTimes.get(day);
            if (times != null && !times.isEmpty()) {
                LocalDateTime time = times.stream()
                    .map(stored -> LocalDateTime.of(adjusted.toLocalDate(), stored))
                    .filter(now::isBefore).min(LocalDateTime::compareTo).orElse(null);

                if (time != null) return time;
            }

            dayCounter++;
        }

        return null;
    }

    public long getClosestTimestamp() {
        LocalDateTime dateTime = this.getClosest();
        return dateTime == null ? -1L : TimeUtil.toEpochMillis(dateTime);
    }*/

    public boolean isReady() {
        DayOfWeek day = TimeUtil.getCurrentDate().getDayOfWeek();
        Set<LocalTime> times = this.getStartTimes().get(day);
        if (times == null || times.isEmpty()) return false;

        LocalTime timeNow = TimeUtil.getCurrentTime().truncatedTo(ChronoUnit.MINUTES);
        return times.stream().anyMatch(stored -> stored.equals(timeNow));
    }

    @NotNull
    public Map<DayOfWeek, Set<LocalTime>> getStartTimes() {
        return startTimes;
    }

    public int getDuration() {
        return duration;
    }
}
