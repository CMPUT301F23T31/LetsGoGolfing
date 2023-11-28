package com.example.letsgogolfing;

import androidx.test.espresso.IdlingResource;
import java.util.concurrent.atomic.AtomicBoolean;

public class FirestoreIdlingResource implements IdlingResource {

    private ResourceCallback resourceCallback;
    private AtomicBoolean isIdleNow = new AtomicBoolean(true);

    @Override
    public String getName() {
        return FirestoreIdlingResource.class.getName();
    }

    @Override
    public boolean isIdleNow() {
        return isIdleNow.get();
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
        resourceCallback = callback;
    }

    // Call this method when Firestore data fetching starts
    public void setIdleState(boolean isIdle) {
        isIdleNow.set(isIdle);
        if (isIdle && resourceCallback != null) {
            resourceCallback.onTransitionToIdle();
        }
    }
}
