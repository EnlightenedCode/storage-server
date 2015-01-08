package com.risevision.storage;

import java.util.List;

import com.risevision.storage.Globals;

import com.google.common.collect.ImmutableList;
import com.google.api.services.storage.model.*;

public final class ObjectAclFactory {
  private ObjectAclFactory() {}

  public static List<ObjectAccessControl> getDefaultAcl() {
    return ImmutableList.of(
      newEntry("allUsers", "READER"),
      newEntry("project-viewers-" + Globals.PROJECT_ID, "READER"),
      newEntry("project-owners-" + Globals.PROJECT_ID, "OWNER"),
      newEntry("project-editors-" + Globals.PROJECT_ID, "OWNER")
    );
  }

  public static List<ObjectAccessControl> getPublicReadEntry() {
    return ImmutableList.of(
      newEntry("allUsers", "READER")
    );
  }

  private static ObjectAccessControl newEntry(String entity, String role) {
    return new ObjectAccessControl().setEntity(entity).setRole(role);
  }
}
