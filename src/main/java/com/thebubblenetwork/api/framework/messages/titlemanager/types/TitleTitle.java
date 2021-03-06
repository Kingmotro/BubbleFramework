package com.thebubblenetwork.api.framework.messages.titlemanager.types;

import com.thebubblenetwork.api.framework.messages.titlemanager.AbstractTitleObject;
import com.thebubblenetwork.api.framework.messages.titlemanager.NMSTitles;
import com.thebubblenetwork.api.framework.messages.titlemanager.TitleType;
import net.minecraft.server.v1_8_R3.IChatBaseComponent;
import net.minecraft.server.v1_8_R3.PacketPlayOutTitle;

public class TitleTitle extends AbstractTitleObject{
    private IChatBaseComponent baseComponent;

    public TitleTitle(String message) {
        super(TitleType.TITLE);
        baseComponent = NMSTitles.toICBC(NMSTitles.toJSON(message));
    }

    public PacketPlayOutTitle create() {
        return new PacketPlayOutTitle(getType().getAction(),baseComponent);
    }
}
