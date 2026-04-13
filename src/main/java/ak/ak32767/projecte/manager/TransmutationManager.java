package ak.ak32767.projecte.manager;

import ak.ak32767.projecte.ProjectE;
import ak.ak32767.projecte.emcsys.EMCBuilder;
import ak.ak32767.projecte.emcsys.ItemConversionBuilder;
import ak.ak32767.projecte.emcsys.WorldTransmutationsBuilder;

import java.util.List;

public class TransmutationManager {
    private final WorldTransmutation worldTransmutation;

    public TransmutationManager(ProjectE plugin) {
        this.worldTransmutation = new WorldTransmutation(plugin);
    }

    public WorldTransmutation getWorldTransmutation() {
        return worldTransmutation;
    }


    public interface TransmutationType {
        boolean build();
        void conversionsRegister(EMCBuilder emcBuilder);
    }

//    public static class CraftTransmutation implements TransmutationType {
//
//    };

    public static class WorldTransmutation implements TransmutationType {
        private final ProjectE plugin;
        private final WorldTransmutationsBuilder builder;

        private WorldTransmutation(ProjectE plugin) {
            this.plugin = plugin;
            this.builder = new WorldTransmutationsBuilder(plugin);
        }

        @Override
        public boolean build() {
            return this.builder.build();
        }

        @Override
        public void conversionsRegister(EMCBuilder emcBuilder) {
            for (var node: this.builder.getConversions()) {
                emcBuilder.register(node.resultForward(), 1).addIngredient(node.origin()).end();

                if (!node.resultForward().equals(node.resultBackward()))
                    emcBuilder.register(node.resultBackward(), 1).addIngredient(node.origin()).end();
            }
        }
    }
}
