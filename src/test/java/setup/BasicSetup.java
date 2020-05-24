package setup;

import io.patriot_framework.hub.PatriotHub;
import io.patriot_framework.hub.PropertiesNotLoadedException;
import io.patriot_framework.junit.extensions.SetupExtension;
import io.patriot_framework.network.simulator.api.builder.TopologyBuilder;
import io.patriot_framework.network.simulator.api.manager.Manager;
import io.patriot_framework.network.simulator.api.model.Topology;
import io.patriot_framework.network_simulator.docker.control.DockerController;

import java.util.Collections;
import java.util.UUID;

public class BasicSetup extends SetupExtension {

    private static UUID uuid = UUID.randomUUID();
    private static boolean configured = false;
    private Topology topology;
    @Override
    protected boolean isSetUp() {
        return configured;
    }

    @Override
    public void setUp() {
        configured = true;
        TopologyBuilder builder = new TopologyBuilder(2)
                .withCreator("Docker")
                .withRouters()
                .withName("MainRt")
                .withCorner(true)
                .createRouter()
                .addRouters()
                .withNetwork("tNet")
                .withIP("192.168.6.0")
                .withMask(24)
                .create()
                .withNetwork("iNet")
                .withInternet(true)
                .create();
        builder.withRoutes()
                .withSourceNetwork("tNet")
                .withDestNetwork("iNet")
                .withCost(1)
                .viaRouter("MainRt")
                .addRoute()
                .buildRoutes();
        topology = builder.build();
        Manager networkManager = null;
        try {
            networkManager = PatriotHub.getInstance().getManager();
            networkManager.setControllers(Collections.singletonList(new DockerController()));
            networkManager.deployTopology(topology);
        } catch (PropertiesNotLoadedException e) {
            e.printStackTrace();
        }

        //do the actual configuration
    }

    @Override
    public void tearDown() {
        Manager networkManager = null;
        try {
            networkManager = PatriotHub.getInstance().getManager();
            networkManager.cleanUp(topology);
        } catch (PropertiesNotLoadedException e) {
            e.printStackTrace();
        }
        //clean up
    }

    @Override
    protected UUID getUUID() {
        return uuid;
    }
}