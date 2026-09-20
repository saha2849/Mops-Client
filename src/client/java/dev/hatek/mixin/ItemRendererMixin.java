package dev.hatek.mixin;

import dev.hatek.client.module.impl.render.SwordTexture;
import net.minecraft.client.renderer.ItemModelResolver;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import javax.imageio.ImageIO;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

@Mixin(ItemModelResolver.class)
public class ItemRendererMixin {
    private static Identifier customSwordTexture = null;
    private static String lastLoadedPath = "";

    @Inject(method = "resolveItemModel", at = @At("RETURN"))
    private void onResolveItemModel(ItemStack stack, CallbackInfoReturnable<BakedModel> cir) {
        SwordTexture module = SwordTexture.instance();
        if (module == null || !module.hasCustomTexture()) {
            return;
        }

        if (!stack.is(Items.WOODEN_SWORD) && !stack.is(Items.STONE_SWORD) && 
            !stack.is(Items.IRON_SWORD) && !stack.is(Items.GOLDEN_SWORD) && 
            !stack.is(Items.DIAMOND_SWORD) && !stack.is(Items.NETHERITE_SWORD)) {
            return;
        }

        String texturePath = module.texturePath();
        if (texturePath == null || texturePath.isEmpty()) {
            return;
        }

        try {
            if (!texturePath.equals(lastLoadedPath) || customSwordTexture == null) {
                Path path = Path.of(texturePath);
                if (Files.isRegularFile(path)) {
                    BufferedImage image = ImageIO.read(path.toFile());
                    if (image != null) {
                        int width = image.getWidth();
                        int height = image.getHeight();
                        
                        NativeImage nativeImage = new NativeImage(NativeImage.Format.RGBA, width, height, false);
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                nativeImage.setPixel(x, y, image.getRGB(x, y));
                            }
                        }
                        
                        customSwordTexture = Identifier.fromNamespaceAndPath("hatek_client", "custom_sword_texture");
                        Minecraft.getInstance().getTextureManager()
                            .register(customSwordTexture, new DynamicTexture(() -> "hatek/custom_sword", nativeImage));
                        lastLoadedPath = texturePath;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
