package lain.mods.cos.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.architectury.platform.Platform;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfigs {

        private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
        private static ClientConfig clientConfig;
        private static CommonConfig commonConfig;

        private static final int DEFAULT_GUI_BUTTON_LEFT = 65;
        private static final int DEFAULT_GUI_BUTTON_TOP = 67;
        private static final int DEFAULT_TOGGLE_BUTTON_LEFT = 59;
        private static final int DEFAULT_TOGGLE_BUTTON_TOP = 72;
        private static final int DEFAULT_CREATIVE_GUI_BUTTON_LEFT = 95;
        private static final int DEFAULT_CREATIVE_GUI_BUTTON_TOP = 38;

        public static boolean getCosArmorGuiButton_Hidden() {
                return clientConfig != null && clientConfig.CosArmorGuiButton_Hidden;
        }

        public static int getCosArmorGuiButton_Left() {
                return clientConfig != null ? clientConfig.CosArmorGuiButton_Left : DEFAULT_GUI_BUTTON_LEFT;
        }

        public static int getCosArmorGuiButton_Top() {
                return clientConfig != null ? clientConfig.CosArmorGuiButton_Top : DEFAULT_GUI_BUTTON_TOP;
        }

        public static boolean getCosArmorToggleButton_Hidden() {
                return clientConfig != null && clientConfig.CosArmorToggleButton_Hidden;
        }

        public static int getCosArmorToggleButton_Left() {
                return clientConfig != null ? clientConfig.CosArmorToggleButton_Left : DEFAULT_TOGGLE_BUTTON_LEFT;
        }

        public static int getCosArmorToggleButton_Top() {
                return clientConfig != null ? clientConfig.CosArmorToggleButton_Top : DEFAULT_TOGGLE_BUTTON_TOP;
        }

        public static boolean getCosArmorCreativeGuiButton_Hidden() {
                return clientConfig != null && clientConfig.CosArmorCreativeGuiButton_Hidden;
        }

        public static int getCosArmorCreativeGuiButton_Left() {
                return clientConfig != null ? clientConfig.CosArmorCreativeGuiButton_Left
                                : DEFAULT_CREATIVE_GUI_BUTTON_LEFT;
        }

        public static int getCosArmorCreativeGuiButton_Top() {
                return clientConfig != null ? clientConfig.CosArmorCreativeGuiButton_Top
                                : DEFAULT_CREATIVE_GUI_BUTTON_TOP;
        }

        public static boolean getCosArmorKeepThroughDeath() {
                return commonConfig != null && commonConfig.CosArmorKeepThroughDeath;
        }

        public static boolean getCosArmorDisableRecipeBook() {
                return commonConfig != null && commonConfig.CosArmorDisableRecipeBook;
        }

        public static boolean getCosArmorDisableCosHatCommand() {
                return commonConfig != null && commonConfig.CosArmorDisableCosHatCommand;
        }

        public static boolean getCosArmorStackRendering() {
                return clientConfig != null && clientConfig.CosArmorStackRendering;
        }

        public static void registerConfigs() {
                Path configDir = Platform.getConfigFolder();
                loadClientConfig(configDir.resolve("cosmeticarmorreworked-client.json"));
                loadCommonConfig(configDir.resolve("cosmeticarmorreworked-common.json"));
        }

        private static void loadClientConfig(Path path) {
                if (Files.exists(path)) {
                        try {
                                clientConfig = GSON.fromJson(Files.readString(path), ClientConfig.class);
                        } catch (IOException e) {
                                ModObjects.logger.error("Failed to load client config", e);
                                clientConfig = new ClientConfig();
                        }
                } else {
                        clientConfig = new ClientConfig();
                        saveClientConfig(path);
                }
        }

        private static void saveClientConfig(Path path) {
                try {
                        Files.writeString(path, GSON.toJson(clientConfig));
                } catch (IOException e) {
                        ModObjects.logger.error("Failed to save client config", e);
                }
        }

        private static void loadCommonConfig(Path path) {
                if (Files.exists(path)) {
                        try {
                                commonConfig = GSON.fromJson(Files.readString(path), CommonConfig.class);
                        } catch (IOException e) {
                                ModObjects.logger.error("Failed to load common config", e);
                                commonConfig = new CommonConfig();
                        }
                } else {
                        commonConfig = new CommonConfig();
                        saveCommonConfig(path);
                }
        }

        private static void saveCommonConfig(Path path) {
                try {
                        Files.writeString(path, GSON.toJson(commonConfig));
                } catch (IOException e) {
                        ModObjects.logger.error("Failed to save common config", e);
                }
        }

        private static class ClientConfig {
                boolean CosArmorGuiButton_Hidden = false;
                int CosArmorGuiButton_Left = DEFAULT_GUI_BUTTON_LEFT;
                int CosArmorGuiButton_Top = DEFAULT_GUI_BUTTON_TOP;
                boolean CosArmorToggleButton_Hidden = false;
                int CosArmorToggleButton_Left = DEFAULT_TOGGLE_BUTTON_LEFT;
                int CosArmorToggleButton_Top = DEFAULT_TOGGLE_BUTTON_TOP;
                boolean CosArmorCreativeGuiButton_Hidden = false;
                int CosArmorCreativeGuiButton_Left = DEFAULT_CREATIVE_GUI_BUTTON_LEFT;
                int CosArmorCreativeGuiButton_Top = DEFAULT_CREATIVE_GUI_BUTTON_TOP;
                boolean CosArmorStackRendering = false;
        }

        private static class CommonConfig {
                boolean CosArmorKeepThroughDeath = false;
                boolean CosArmorDisableRecipeBook = false;
                boolean CosArmorDisableCosHatCommand = false;
        }

}
