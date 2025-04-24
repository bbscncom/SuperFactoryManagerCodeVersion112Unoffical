package ca.teamdman.sfm.client.render;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.VertexFormat;

import java.util.Arrays;

public class RetexturedBakedQuad extends BakedQuad {

    private final TextureAtlasSprite texture;

    public RetexturedBakedQuad(BakedQuad quad, TextureAtlasSprite textureIn) {
        super(Arrays.copyOf(quad.getVertexData(), quad.getVertexData().length), quad.getTintIndex()
                , quad.getFace(), quad.getSprite(), quad.shouldApplyDiffuseLighting(),new VertexFormat());
        this.texture = textureIn;
        this.remapQuad();
    }

    private void remapQuad() {
        for (int i = 0; i < 4; ++i) {
            int j = 7 * i;
            this.vertexData[j + 4] = Float.floatToRawIntBits(this.texture.getInterpolatedU(getUnInterpolatedU(this.sprite, Float.intBitsToFloat(this.vertexData[j + 4]))));
            this.vertexData[j + 5] = Float.floatToRawIntBits(this.texture.getInterpolatedV(getUnInterpolatedV(this.sprite, Float.intBitsToFloat(this.vertexData[j + 5]))));
        }
    }

    @Override
    public TextureAtlasSprite getSprite() {
        return texture;
    }

    private static float getUnInterpolatedU(TextureAtlasSprite sprite, float u) {
        float f = sprite.getMaxU() - sprite.getMinU();
        return (u - sprite.getMinU()) / f;
    }

    private static float getUnInterpolatedV(TextureAtlasSprite sprite, float v) {
        float f = sprite.getMaxV() - sprite.getMinV();
        return (v - sprite.getMinV()) / f;
    }
}
