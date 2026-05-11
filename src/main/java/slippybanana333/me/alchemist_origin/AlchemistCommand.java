package slippybanana333.me.alchemist_origin;

import com.mojang.brigadier.context.CommandContext;
import io.github.apace100.apoli.component.PowerHolderComponent;
import io.github.apace100.apoli.power.PowerTypeRegistry;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

/**
 * Registers the /alc command.
 *
 * /alc — opens the Alchemist effect-selection GUI for the executing player.
 * Requires the player to have the Alchemist origin; denies access otherwise.
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

        boolean isAlchemist = PowerHolderComponent.KEY.get(player)
                .hasPower(PowerTypeRegistry.get(new Identifier("alchemist_origin", "reduced_health")));

        if (!isAlchemist) {
            source.sendError(Text.literal("You must be an Alchemist to use this command."));
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

