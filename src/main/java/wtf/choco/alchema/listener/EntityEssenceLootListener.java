package wtf.choco.alchema.listener;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import wtf.choco.alchema.Alchema;
import wtf.choco.alchema.api.event.entity.EntityDropEssenceEvent;
import wtf.choco.alchema.essence.EntityEssenceData;
import wtf.choco.alchema.util.AlchemaEventFactory;

public final class EntityEssenceLootListener implements Listener {

    private final Alchema plugin;

    public EntityEssenceLootListener(@NotNull Alchema plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    private void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        EntityEssenceData essenceData = plugin.getEntityEssenceEffectRegistry().getEntityEssenceData(entity.getType());
        if (essenceData == null) {
            return;
        }

        int lootingModifier = 0;

        if (entity instanceof LivingEntity) {
            Player killer = ((LivingEntity) entity).getKiller();
            if (killer != null) {
                ItemStack item = killer.getInventory().getItemInMainHand();
                lootingModifier = item.getEnchantmentLevel(Enchantment.LOOT_BONUS_MOBS);
            }
        }

        ThreadLocalRandom random = ThreadLocalRandom.current();
        if (random.nextDouble() * 100.0 >= 0.75 + (lootingModifier * 0.25)) { // 0.75% chance
            return;
        }

        // TODO: Make this amount of essence configurable and slightly random
        EntityDropEssenceEvent entityDropEssenceEvent = AlchemaEventFactory.callEntityDropEssenceEvent(entity, essenceData, 50);
        if (entityDropEssenceEvent.isCancelled()) {
            return;
        }

        event.getDrops().add(essenceData.createItemStack(entityDropEssenceEvent.getAmountOfEssence()));
    }

}
