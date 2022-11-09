import io.gerhardt.keycloaklivebackup.utilities.PrometheusExporter;
import io.prometheus.client.CollectorRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.lang.reflect.Field;

public class PrometheusExporterTest {

    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

    @Before
    public void resetSingleton() throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Field instance = PrometheusExporter.class.getDeclaredField("INSTANCE");
        instance.setAccessible(true);
        instance.set(null, null);
        CollectorRegistry.defaultRegistry.clear();
    }

    @Test
    public void shouldBuildPushgateway() {
        final String envVar = "PROMETHEUS_PUSHGATEWAY_ADDRESS";
        final String address = "localhost:9091";
        environmentVariables.set(envVar, address);
        Assert.assertNotNull(PrometheusExporter.instance().PUSH_GATEWAY);
    }

    @Test
    public void shouldBuildPushgatewayWithHttps() {
        final String envVar = "PROMETHEUS_PUSHGATEWAY_ADDRESS";
        final String address = "https://localhost:9091";
        environmentVariables.set(envVar, address);
        Assert.assertNotNull(PrometheusExporter.instance().PUSH_GATEWAY);
    }

    @Test
    public void shouldNotBuildPushgateway() {
        Assert.assertNull(PrometheusExporter.instance().PUSH_GATEWAY);
    }

    private static final class Tuple<L, R> {
        final L left;
        final R right;

        private Tuple(L left, R right) {
            this.left = left;
            this.right = right;
        }
    }
}
