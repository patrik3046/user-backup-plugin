package io.gerhardt.keycloaklivebackup.utilities;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Counter;
import io.prometheus.client.Gauge;
import io.prometheus.client.exporter.PushGateway;
import io.prometheus.client.exporter.common.TextFormat;
import org.jboss.logging.Logger;

import java.io.*;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PrometheusExporter {

    private final Pattern PROMETHEUS_PUSHGATEWAY_GROUPINGKEY_INSTANCE_ENVVALUE_PATTERN = Pattern.compile("ENVVALUE:(.+?)");

    private static PrometheusExporter INSTANCE;

    private final Logger logger = Logger.getLogger(PrometheusExporter.class);

    // these fields are package private on purpose
    static Gauge numberOfJsonFiles;
    static Counter numberOfUpdatedJsonFiles;
    static Counter numberOfDeletedJsonFiles;
    static Counter totalNumberOfEvents;
    public PushGateway PUSH_GATEWAY;

    private PrometheusExporter() {
        // The metrics collector needs to be a singleton because requiring a
        // provider from the KeyCloak session (session#getProvider) will always
        // create a new instance. Not sure if this is a bug in the SPI implementation
        // or intentional but better to avoid this. The metrics object is single-instance
        // anyway and all the Gauges are suggested to be static (it does not really make
        // sense to record the same metric in multiple places)

        PUSH_GATEWAY = buildPushGateWay();

        // package private on purpose
        numberOfJsonFiles = Gauge.build()
                .name("number_of_JSON_files")
                .help("Exported .json files with user data in it.")
                .labelNames("realm")
                .register();

        numberOfUpdatedJsonFiles = Counter.build()
                .name("number_of_Updated_JSON_files")
                .help("The number of the updated .json files in the Directory.")
                .labelNames("realm")
                .register();

        numberOfDeletedJsonFiles = Counter.build()
                .name("number_of_deleted_JSON_files")
                .help("The number of the deleted .json files in the Directory.")
                .labelNames("realm")
                .register();

        totalNumberOfEvents = Counter.build()
                .name("total_number_of_events")
                .help("Total number of user and admin events")
                .labelNames("realm")
                .register();
    }

    public static synchronized PrometheusExporter instance() {
        if (INSTANCE == null) {
            INSTANCE = new PrometheusExporter();
        }
        return INSTANCE;
    }

    /**
     * Increase the number of user events
     *
     * @param realmId name of the realm
     */
    public void recordEvents(final String realmId) {
        totalNumberOfEvents.labels(realmId).inc();
        pushAsync();
    }

    /**
     * Increase the number of the .json files
     *
     * @param realmId name of the realm
     */
    public void recordJsonFilesNumber(final String realmId) {
        JsonManager jsonManager = new JsonManager(new UserDataManager());
        int jsonFilesNumber = jsonManager.getNumberOfJsonFiles();
        numberOfJsonFiles.labels(realmId).set(jsonFilesNumber);
    }

    /**
     * Increase the number of the updated .json files
     *
     * @param realmId name of the realm
     */

    public void recordUpdatedJsonFilesNumber(final String realmId) {
        numberOfUpdatedJsonFiles.labels(realmId).inc();
    }

    /**
     * Increase the number of the deleted .json files
     *
     * @param realmId name of the realm
     */
    public void recordDeletedJsonFilesNumber(final String realmId) {
        numberOfDeletedJsonFiles.labels(realmId).inc();
    }

    /**
     * Write the Prometheus formatted values of all counters and
     * gauges to the stream
     *
     * @param stream Output stream
     */
    public void export(final OutputStream stream) throws IOException {
        final Writer writer = new BufferedWriter(new OutputStreamWriter(stream));
        TextFormat.write004(writer, CollectorRegistry.defaultRegistry.metricFamilySamples());
        writer.flush();
    }

    /**
     * Build a prometheus pushgateway if an address is defined in environment.
     *
     * @return PushGateway
     */
    private PushGateway buildPushGateWay() {
        // host:port or ip:port of the Push gateway.
        PushGateway pg = null;
        String host = System.getenv("PROMETHEUS_PUSHGATEWAY_ADDRESS");
        if (host != null) {
            // if protocol is missing in host, we assume http
            if (!host.toLowerCase().startsWith("http://") && !host.startsWith("https://")) {
                host = "http://" + host;
            }
            try {
                pg = new PushGateway(new URL(host));
                logger.info("Push gateway created with url " + host + ".");
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
        }
        return pg;
    }

    public void pushAsync() {
        CompletableFuture.runAsync(this::push);
    }

    private String instanceIp() throws UnknownHostException {
        return InetAddress.getLocalHost().getHostAddress();
    }

    private String groupingKey() throws UnknownHostException {
        String PROMETHEUS_PUSHGATEWAY_GROUPINGKEY_INSTANCE = "PROMETHEUS_GROUPING_KEY_INSTANCE";
        return Optional.ofNullable(System.getenv(PROMETHEUS_PUSHGATEWAY_GROUPINGKEY_INSTANCE))
                .map(envValue -> {
                    Matcher matcher = PROMETHEUS_PUSHGATEWAY_GROUPINGKEY_INSTANCE_ENVVALUE_PATTERN.matcher(envValue);
                    if (matcher.matches()) return System.getenv(matcher.group(1));
                    else return envValue;
                }).orElse(instanceIp());
    }

    private void push() {
        if (PUSH_GATEWAY != null) {
            try {
                String PROMETHEUS_PUSHGATEWAY_JOB = "PROMETHEUS_PUSHGATEWAY_JOB";
                String job = Optional.ofNullable(System.getenv(PROMETHEUS_PUSHGATEWAY_JOB)).orElse("keycloak");
                Map<String, String> groupingKey = Collections.singletonMap("instance", groupingKey());
                PUSH_GATEWAY.pushAdd(CollectorRegistry.defaultRegistry, job, groupingKey);
            } catch (IOException e) {
                logger.error("Unable to send to prometheus PushGateway", e);
            }
        }
    }
}
