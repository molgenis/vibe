package org.molgenis.vibe.core;

public enum TestConstants {
    DISGENET_VERSION {
        @Override
        public String get() {
            return "v7.0.0";
        }
    };

    /**
     * Returns the test constant.
     * @return {@link String}
     */
    public abstract String get();
}
