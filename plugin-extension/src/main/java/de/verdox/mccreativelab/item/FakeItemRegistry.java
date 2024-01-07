package de.verdox.mccreativelab.item;

import de.verdox.mccreativelab.registry.CustomRegistry;

public class FakeItemRegistry extends CustomRegistry<FakeItem> {
    public <T extends FakeItem> T register(FakeItem.Builder<T> fakeItemBuilder){
        T fakeItem = fakeItemBuilder.buildItem();
        register(fakeItem.getKey(), fakeItem);
        return fakeItem;
    }
}
