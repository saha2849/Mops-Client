package dev.hatek.client.module.impl.render;

import dev.hatek.client.module.Category;
import dev.hatek.client.module.Module;
import dev.hatek.client.module.setting.TextSetting;
import dev.hatek.client.module.setting.ButtonSetting;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;

public final class SwordTexture extends Module {
    private static SwordTexture instance;

    private final TextSetting texturePath = new TextSetting("Texture Path", "", "path to sword texture.png");
    private final ButtonSetting browse = new ButtonSetting("Browse...", this::browseFile);

    private String activeTexturePath = "";

    public SwordTexture() {
        super("Sword Texture", "Change sword texture to custom image", Category.RENDER);
        instance = this;
        with(this.texturePath, this.browse);
    }

    public static SwordTexture instance() {
        return instance;
    }

    public String getActiveTexturePath() {
        if (isEnabled() && !this.texturePath.value().isBlank()) {
            return this.texturePath.value().trim();
        }
        return "";
    }

    private void browseFile() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null) {
            mc.gui.chatListener().handleSystemMessage(
                    net.minecraft.network.chat.Component.literal("§8[§fHatek§8] §7Укажите путь к PNG файлу текстуры"), false);
        }
    }

    @Override
    protected void onEnable() {
        String path = this.texturePath.value().trim();
        if (path.isBlank()) {
            sayChat("Укажите путь к текстуре");
            setEnabled(false);
            return;
        }
        if (!Files.isRegularFile(Path.of(path))) {
            sayChat("Текстура не найдена: " + path);
            setEnabled(false);
            return;
        }
        this.activeTexturePath = path;
        sayChat("Текстура меча применена");
    }

    @Override
    protected void onDisable() {
        this.activeTexturePath = "";
        sayChat("Текстура меча отключена");
    }

    public boolean hasCustomTexture() {
        return isEnabled() && !this.activeTexturePath.isBlank();
    }

    public String texturePath() {
        return this.activeTexturePath;
    }

    private void sayChat(String text) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null) {
            mc.gui.chatListener().handleSystemMessage(
                    net.minecraft.network.chat.Component.literal("§8[§fHatek§8] §7" + text), false);
        }
    }
}
