package io.github.moehreag.axolotlclient.config.options;

import com.google.gson.JsonElement;
import net.minecraft.client.resource.language.I18n;

public interface Option {

    OptionType getType();

    String getName();

    default String getTranslatedName(){
        return I18n.translate(this.getName());
    }

    void setValueFromJsonElement(JsonElement element);

    void setDefaults();

    JsonElement getJson();
}