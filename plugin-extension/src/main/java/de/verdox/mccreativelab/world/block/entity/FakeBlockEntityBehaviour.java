package de.verdox.mccreativelab.world.block.entity;

import de.verdox.mccreativelab.behaviour.BehaviourResult;
import de.verdox.mccreativelab.behaviour.entity.EntityBehaviour;
import org.bukkit.entity.Marker;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class FakeBlockEntityBehaviour implements EntityBehaviour<Marker> {
    private final FakeBlockEntity fakeBlockEntity;
    public FakeBlockEntityBehaviour(@NotNull FakeBlockEntity fakeBlockEntity){
        Objects.requireNonNull(fakeBlockEntity);
        this.fakeBlockEntity = fakeBlockEntity;
    }
    @Override
    public BehaviourResult.Callback onTick(Marker entity) {
        fakeBlockEntity.doTick();
        return done();
    }
}
