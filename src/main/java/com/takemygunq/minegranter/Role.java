package com.takemygunq.minegranter;

/** A staff role and its two LuckPerms groups: one for on shift, one for off shift. */
public record Role(String id, String prefix, String onGroup, String offGroup) {
}
