package de.xenoworld.senseless;

import com.fasterxml.jackson.core.type.TypeReference;
import de.xenoworld.senseless.config.CollectionConfig;
import de.xenoworld.senseless.config.ValueConfig;
import io.javalin.Javalin;
import io.javalin.http.Context;
import io.javalin.plugin.json.JavalinJackson;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static de.xenoworld.senseless.config.ValueConfig.Type.INTEGER;

public class Main {
    private static final ConcurrentHashMap<String, Collection> collections = new ConcurrentHashMap<>();
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private static String FILENAME_DUMP;
    private static String FILENAME_CONFIG;

    public static void main(String[] args) throws IOException {
        FILENAME_CONFIG = System.getProperty("senseless.config", "collections.json");
        FILENAME_DUMP = System.getProperty("senseless.dump", "data.json");

        // TODO: In case we save Instants or something.
        // JavalinJackson.getObjectMapper().findAndRegisterModules();

        var app = Javalin.create().start(7000);

        collections.putAll(loadConfiguration());
        loadData();

        app.post("/*", Main::updateValue);
        app.get("/*", Main::getData);

        // TODO: Is this the right approach to periodic saving?
        executorService.scheduleWithFixedDelay(Main::dumpData, 1, 15, TimeUnit.MINUTES);
        Runtime.getRuntime().addShutdownHook(new Thread(Main::dumpData));
    }

    /**
     * Set data on a collection.
     * <p>
     * The Collection can optionally be protected with a writeToken.
     */
    private static void updateValue(Context context) {
        final var token = context.queryParam("token");
        final var path = stripTrailingSlashes(context.path());

        Collection c = findCollection(context, path);

        if (c == null) {
            return;
        }

        if (!isAllowed(context, token, c.writeToken())) {
            return;
        }

        var value = c.value(context.path().substring(c.prefix().length()));

        if (value == null) {
            c.valueUsingDefaults(path.substring(c.prefix().length()), new DoubleValue((Double.parseDouble(context.body()))));
        } else {
            value.value(context.body());
        }
    }

    /**
     * See if there is an collection for the given path.
     */
    @Nullable
    private static Collection findCollection(Context context, String path) {
        var ep = mostSpecific(path);

        if (ep == null) {
            context.status(404).result("Not found.");
            return null;
        }

        return ep;
    }

    /**
     * Get data from an collection.
     * <p>
     * The Collection can optionally be protected with a readToken.
     */
    private static void getData(Context context) {
        final var token = context.queryParam("token");
        final var path = stripTrailingSlashes(context.path());

        Collection ep = findCollection(context, path);

        if (ep == null) {
            return;
        }

        if (!isAllowed(context, token, ep.readToken())) {
            return;
        }

        var subPath = path.replaceFirst(ep.prefix(), "");

        // TODO: This warrants a test ;-)
        context.json(ep.values().entrySet().parallelStream().filter(entry -> entry.getKey().startsWith(subPath)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
    }

    /**
     * Assure that, if a token is set, the provided token matches it.
     */
    private static boolean isAllowed(Context context, String providedToken, Optional<String> wantedToken) {
        if (wantedToken.isEmpty()) {
            return true;
        }

        if (providedToken == null) {
            context.status(403).result("No access token");
            return false;
        }

        if (!providedToken.equals(wantedToken.get())) {
            context.status(403).result("Wrong access token");
            return false;
        }

        return true;
    }

    private static Map<String, Collection> loadConfiguration() throws IOException {
        var configFile = new File(FILENAME_CONFIG);

        CollectionConfig[] collectionConfigList;
        collectionConfigList = JavalinJackson.getObjectMapper().readValue(configFile, CollectionConfig[].class);

        var result = new HashMap<String, Collection>();

        for (CollectionConfig config : collectionConfigList) {
            Collection c = new Collection(config.path);
            c.readToken(config.readToken);
            c.writeToken(config.writeToken);

            result.put(c.prefix(), c);

            if (config.valueConfig == null) {
                continue;
            }

            for (ValueConfig valueConfig : config.valueConfig) {
                Value value = valueConfig.type == INTEGER ? new IntegerValue() : new DoubleValue();

                if (valueConfig.lifetime != null) {
                    value.lifetime = valueConfig.lifetime;
                }

                value.operations(valueConfig);
                c.value(valueConfig.path, value);
            }
        }

        return result;
    }

    private static void dumpData() {
        try {
            var dumpFile = new File(FILENAME_DUMP);
            JavalinJackson.getObjectMapper().writeValue(dumpFile, collections);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void loadData() {
        // TODO: Also save and restore unconfigured data
        var dumpFile = new File(FILENAME_DUMP);
        try {
            HashMap<String, Collection> data;
            data = JavalinJackson.getObjectMapper().readValue(dumpFile, new TypeReference<HashMap<String, Collection>>() {
            });

            for (String prefix : data.keySet()) {
                if (!collections.containsKey(prefix)) {
                    continue;
                }

                var collection = collections.get(prefix);
                var dataForCollection = data.get(prefix);

                for (String path : dataForCollection.values().keySet()) {
                    if (!collection.values().containsKey(path)) {
                        continue;
                    }

                    if (!dataForCollection.values().containsKey(path)) {
                        continue;
                    }

                    var value = collection.values().get(path);
                    var dataForValue = dataForCollection.values().get(path);

                    if (value instanceof DoubleValue && dataForValue instanceof DoubleValue) {
                        var convertedValue = (DoubleValue) value;
                        var convertedData = (DoubleValue) dataForValue;

                        convertedValue.rawValue = convertedData.rawValue;
                        convertedValue.rawMax = convertedData.rawMax;
                        convertedValue.rawMin = convertedData.rawMin;
                        convertedValue.tsOfMinValue = convertedData.tsOfMinValue;
                        convertedValue.tsOfMaxValue = convertedData.tsOfMaxValue;

                    } else if (value instanceof IntegerValue && dataForValue instanceof IntegerValue) {
                        var convertedValue = (IntegerValue) value;
                        var convertedData = (IntegerValue) dataForValue;

                        convertedValue.rawValue = convertedData.rawValue;
                        convertedValue.rawMax = convertedData.rawMax;
                        convertedValue.rawMin = convertedData.rawMin;
                        convertedValue.tsOfMinValue = convertedData.tsOfMinValue;
                        convertedValue.tsOfMaxValue = convertedData.tsOfMaxValue;
                    }
                }
            }
        } catch (FileNotFoundException e) {
            Javalin.log.warn(FILENAME_DUMP + " does not exist");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Remove parts of the path until a Collection for the given path is found.
     */
    static Collection mostSpecific(String path) {
        return mostSpecific(collections, path);
    }

    /**
     * Remove parts of the path until a Collection for the given path is found.
     */
    static Collection mostSpecific(Map<String, Collection> collections, String path) {
        var len = path.split("/").length;

        for (int i = 0; i < len; i++) {
            if (collections.containsKey(path)) {
                return collections.get(path);
            }

            path = path.replaceFirst("/[^\\\\/]+$", "");
        }

        return null;
    }

    static String stripTrailingSlashes(String path) {
        return path.replaceAll("/+$", "");
    }
}
