package lain.mods.cos.api.event;

import dev.architectury.event.Event;
import dev.architectury.event.EventFactory;
import lain.mods.cos.api.inventory.CAStacksBase;
import net.minecraft.world.entity.player.Player;

/**
 * This event is fired whenever a player dies and the associated CAStacks is
 * about to be dropped. <br>
 * <br>
 * <p>
 * {@link #player} contains the instance of EntityPlayer for the event.<br>
 * {@link #stacks} contains the instance of CAStacks for the player.<br>
 * <br>
 * If the event handler returns true, the CAStacks for the player will not be
 * altered and nothing will be added to the drops.<br>
 */
public class CosArmorDeathDrops {

    public static final Event<CosArmorDeathDropsCallback> EVENT = EventFactory.createLoop();

    private final Player player;
    private final CAStacksBase stacks;

    public CosArmorDeathDrops(Player player, CAStacksBase stacks) {
        this.player = player;
        this.stacks = stacks;
    }

    public CAStacksBase getCAStacks() {
        return stacks;
    }

    public Player getEntityPlayer() {
        return player;
    }

    @FunctionalInterface
    public interface CosArmorDeathDropsCallback {
        boolean onDeathDrops(CosArmorDeathDrops event);
    }

}
