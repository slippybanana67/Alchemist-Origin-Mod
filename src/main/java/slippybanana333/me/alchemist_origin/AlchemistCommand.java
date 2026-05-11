package slippybanana333.me.alchemist_origin;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * Registers the /alc command.
 *
 * /alc — opens the Alchemist effect-selection GUI for the executing player.
 *
 * Permission: available to all players (requirement level 0).
 * Intended use: players who chose the Alchemist origin use this to pick or
 * change their permanent potion effect.  The Origins JSON powers (reduced
 * health, etc.) already gate drawbacks behind origin selection; this command
 * only grants the beneficial side (chosen effect + debuff immunity) to anyone
 * who invokes it, which is an acceptable design for a co-operative server.
 * If stricter gating is required, add an Origins API check here.
 */
public class AlchemistCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
            dispatcher.register(
                CommandManager.literal("alc")
                    .executes(AlchemistCommand::execute)
            )
        );
    }

    private static int execute(CommandContext<ServerCommandSource> ctx) {
        ServerCommandSource source = ctx.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("Only players can use /alc."));
            return 0;
        }

        AlchemistPlayerData data = AlchemistEffectManager.getOrCreateData(player);
        player.openHandledScreen(new SimpleNamedScreenHandlerFactory(
                (syncId, playerInv, p) -> new AlchemistScreenHandler(syncId, playerInv, data),
                Text.literal("Alchemist: Manage Effects").formatted(Formatting.DARK_PURPLE)
        ));
        return 1;
    }
}

