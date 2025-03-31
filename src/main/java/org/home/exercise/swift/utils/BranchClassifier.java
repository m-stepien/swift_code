package org.home.exercise.swift.utils;

public class BranchClassifier {
    private static final String HEADQUARTER_BRANCH_CODE = "XXX";

    public static boolean isHeadquarter(String swiftCode){
        return swiftCode.length() == 8 ||
                (swiftCode.length()==11 &&swiftCode.endsWith(HEADQUARTER_BRANCH_CODE));
    }
}
