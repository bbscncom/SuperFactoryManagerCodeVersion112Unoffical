package my;


import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.*;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import com.google.gson.*;

public class ToolTextComponentSerializer {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(Style.class, new StyleSerializer())
            .create();

    // 序列化为JSON字符串
    public static String serialize(TextComponentTranslation component) {
        JsonObject json = new JsonObject();

        // 添加基础属性
        json.addProperty("key", component.getKey());

        // 序列化参数
        JsonArray argsArray = new JsonArray();
        for (Object arg : component.getFormatArgs()) {
            argsArray.add(serializeArg(arg));
        }
        json.add("args", argsArray);

        // 序列化样式
        if (component.getStyle() != null && !component.getStyle().isEmpty()) {
            json.add("style", GSON.toJsonTree(component.getStyle()));
        }

        return GSON.toJson(json);
    }

    // 反序列化
    public static TextComponentTranslation deserialize(String jsonStr) {
        JsonObject json = GSON.fromJson(jsonStr, JsonObject.class);

        // 获取key
        String key = json.get("key").getAsString();

        // 反序列化参数
        JsonArray argsArray = json.getAsJsonArray("args");
        Object[] args = new Object[argsArray.size()];
        for (int i = 0; i < argsArray.size(); i++) {
            args[i] = deserializeArg(argsArray.get(i));
        }

        // 创建组件
        TextComponentTranslation component = new TextComponentTranslation(key, args);

        // 反序列化样式
        if (json.has("style")) {
            Style style = GSON.fromJson(json.get("style"), Style.class);
            component.setStyle(style);
        }

        return component;
    }

    // 序列化单个参数
    private static JsonElement serializeArg(Object arg) {
        if (arg == null) {
            return JsonNull.INSTANCE;
        } else if (arg instanceof String) {
            return new JsonPrimitive((String) arg);
        } else if (arg instanceof Number) {
            return new JsonPrimitive((Number) arg);
        } else if (arg instanceof Boolean) {
            return new JsonPrimitive((Boolean) arg);
        } else if (arg instanceof ITextComponent) {
            return new JsonPrimitive(ITextComponent.Serializer.componentToJson((ITextComponent) arg));
        } else if (arg instanceof ItemStack) {
            NBTTagCompound nbt = new NBTTagCompound();
            ((ItemStack) arg).writeToNBT(nbt);
            return new JsonPrimitive(nbt.toString());
        } else {
            // 默认使用toString()
            return new JsonPrimitive(arg.toString());
        }
    }

    // 反序列化单个参数
    private static Object deserializeArg(JsonElement element) {
        if (element.isJsonNull()) {
            return null;
        } else if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (primitive.isString()) {
                String str = primitive.getAsString();
                // 尝试解析为ITextComponent
                try {
                    return ITextComponent.Serializer.jsonToComponent(str);
                } catch (JsonParseException e) {
                    return str;
                }
            } else if (primitive.isNumber()) {
                return primitive.getAsNumber();
            } else if (primitive.isBoolean()) {
                return primitive.getAsBoolean();
            }
        }
        return element.getAsString();
    }

    // Style的序列化适配器
    private static class StyleSerializer implements JsonSerializer<Style>, JsonDeserializer<Style> {
        @Override
        public JsonElement serialize(Style src, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            if (src.getColor() != null) {
                json.addProperty("color", src.getColor().getFriendlyName());
            }
            if (src.getBold()) {
                json.addProperty("bold", true);
            }
            if (src.getItalic()) {
                json.addProperty("italic", true);
            }
            if (src.getUnderlined()) {
                json.addProperty("underlined", true);
            }
            if (src.getStrikethrough()) {
                json.addProperty("strikethrough", true);
            }
            if (src.getObfuscated()) {
                json.addProperty("obfuscated", true);
            }
            if (src.getClickEvent() != null) {
                JsonObject click = new JsonObject();
                click.addProperty("action", src.getClickEvent().getAction().getCanonicalName());
                click.addProperty("value", src.getClickEvent().getValue());
                json.add("clickEvent", click);
            }
            if (src.getHoverEvent() != null) {
                JsonObject hover = new JsonObject();
                hover.addProperty("action", src.getHoverEvent().getAction().getCanonicalName());
                hover.add("value", context.serialize(src.getHoverEvent().getValue()));
                json.add("hoverEvent", hover);
            }
            return json;
        }

        @Override
        public Style deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject obj = json.getAsJsonObject();
            Style style = new Style();

            if (obj.has("color")) {
                style.setColor(TextFormatting.getValueByName(obj.get("color").getAsString()));
            }
            if (obj.has("bold")) {
                style.setBold(obj.get("bold").getAsBoolean());
            }
            if (obj.has("italic")) {
                style.setItalic(obj.get("italic").getAsBoolean());
            }
            if (obj.has("underlined")) {
                style.setUnderlined(obj.get("underlined").getAsBoolean());
            }
            if (obj.has("strikethrough")) {
                style.setStrikethrough(obj.get("strikethrough").getAsBoolean());
            }
            if (obj.has("obfuscated")) {
                style.setObfuscated(obj.get("obfuscated").getAsBoolean());
            }
            if (obj.has("clickEvent")) {
                JsonObject click = obj.getAsJsonObject("clickEvent");
                ClickEvent.Action action = ClickEvent.Action.getValueByCanonicalName(click.get("action").getAsString());
                style.setClickEvent(new ClickEvent(action, click.get("value").getAsString()));
            }
            if (obj.has("hoverEvent")) {
                JsonObject hover = obj.getAsJsonObject("hoverEvent");
                HoverEvent.Action action = HoverEvent.Action.getValueByCanonicalName(hover.get("action").getAsString());
                ITextComponent value = context.deserialize(hover.get("value"), ITextComponent.class);
                style.setHoverEvent(new HoverEvent(action, value));
            }

            return style;
        }
    }
}