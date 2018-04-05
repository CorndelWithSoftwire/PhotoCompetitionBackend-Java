package org.softwire.training.models;

import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.core.mapper.reflect.JdbiConstructor;

public class User {
    private final String uuid;
    private final String name;

    @JdbiConstructor
    public User(@ColumnName("uuid") String uuid,
                @ColumnName("name") String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }
}
