package de.verdox.mccreativelab.behaviour;

public interface Behaviour {
    default BehaviourResult.Void voidResult(BehaviourResult.Void.Type type) {
        return new BehaviourResult.Void(type);
    }

    default BehaviourResult.Void voidResult() {
        return voidResult(BehaviourResult.Void.Type.REPLACE_VANILLA);
    }

    default BehaviourResult.Callback done() {
        return new BehaviourResult.Callback();
    }

    default BehaviourResult.Bool bool(boolean value, BehaviourResult.Bool.Type type) {
        return new BehaviourResult.Bool(value, type);
    }

    default BehaviourResult.Bool bool(boolean value) {
        return bool(value, BehaviourResult.Bool.Type.REPLACE_VANILLA);
    }

    default <T> BehaviourResult.Object<T> result(T result, BehaviourResult.Object.Type type){
        return new BehaviourResult.Object<>(result, type);
    }

    default <T> BehaviourResult.Object<T> result(T result){
        return result(result, BehaviourResult.Object.Type.REPLACE_VANILLA);
    }
}
