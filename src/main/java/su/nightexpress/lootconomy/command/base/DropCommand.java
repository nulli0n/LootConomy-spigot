package su.nightexpress.lootconomy.command.base;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import su.nexmedia.engine.api.command.AbstractCommand;
import su.nexmedia.engine.api.command.CommandResult;
import su.nexmedia.engine.lang.EngineLang;
import su.nexmedia.engine.utils.CollectionsUtil;
import su.nexmedia.engine.utils.NumberUtil;
import su.nexmedia.engine.utils.random.Rnd;
import su.nightexpress.lootconomy.LootConomy;
import su.nightexpress.lootconomy.config.Perms;
import su.nightexpress.lootconomy.Placeholders;
import su.nightexpress.lootconomy.api.currency.Currency;
import su.nightexpress.lootconomy.config.Lang;
import su.nightexpress.lootconomy.money.MoneyManager;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DropCommand extends AbstractCommand<LootConomy> {

    public DropCommand(@NotNull LootConomy plugin) {
        super(plugin, new String[]{"drop"}, Perms.COMMAND_DROP);
        this.setDescription(plugin.getMessage(Lang.COMMAND_DROP_DESC));
        this.setUsage(plugin.getMessage(Lang.COMMAND_DROP_USAGE));
    }

    @Override
    @NotNull
    public List<String> getTab(@NotNull Player player, int arg, @NotNull String[] args) {
        if (arg == 1) {
            return plugin.getCurrencyManager().getCurrencies().stream().map(Currency::getId).toList();
        }
        if (arg == 2) {
            return Arrays.asList("10", "100");
        }
        if (arg == 3) {
            return Arrays.asList("10", "100", "20", "500");
        }
        if (arg == 4) {
            return CollectionsUtil.worldNames();
        }
        if (arg == 5) {
            return Collections.singletonList(NumberUtil.format(player.getLocation().getX()));
        }
        if (arg == 6) {
            return Collections.singletonList(NumberUtil.format(player.getLocation().getY()));
        }
        if (arg == 7) {
            return Collections.singletonList(NumberUtil.format(player.getLocation().getZ()));
        }
        return super.getTab(player, arg, args);
    }

    @Override
    protected void onExecute(@NotNull CommandSender sender, @NotNull CommandResult result) {
        if (result.length() < 8) {
            this.printUsage(sender);
            return;
        }

        Currency currency = plugin.getCurrencyManager().getCurrency(result.getArg(1));
        if (currency == null) {
            plugin.getMessage(Lang.CURRENCY_ERROR_INVALID).send(sender);
            return;
        }

        World world = plugin.getServer().getWorld(result.getArg(4));
        if (world == null) {
            plugin.getMessage(EngineLang.ERROR_WORLD_INVALID).send(sender);
            return;
        }

        double min = result.getDouble(2, 0D);
        double max = result.getDouble(3, 0D);
        double amount = Rnd.getDouble(min, max);
        if (amount <= 0) return;

        ItemStack item = MoneyManager.createMoney(currency, amount, null, null, null);

        double x = result.getDouble(5, 0);
        double y = result.getDouble(6, 0);
        double z = result.getDouble(7, 0);

        Location location = new Location(world, x, y, z);
        world.dropItem(location, item);

        plugin.getMessage(Lang.COMMAND_DROP_DONE)
            .replace(Placeholders.GENERIC_AMOUNT, currency.format(amount))
            .replace(Placeholders.forLocation(location))
            .send(sender);
    }
}
