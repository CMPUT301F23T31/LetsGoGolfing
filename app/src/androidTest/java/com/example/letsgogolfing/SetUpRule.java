package com.example.letsgogolfing;

import com.example.letsgogolfing.utils.DataRepository;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class SetUpRule implements TestRule {

    private static boolean isFirestoreSetupExecuted = false;

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                if (!isFirestoreSetupExecuted) {
                    // One-time setup code for Firestore
                    DataRepository.handleUserCollections(new String[]{"testLogin", "nonUniqueUser"}, true);
                    isFirestoreSetupExecuted = true;
                }
                base.evaluate();
            }
        };
    }
}