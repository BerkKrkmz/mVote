package com.mVote.storage;

import java.util.UUID;

public interface IStorage {
    void setup();
    void addVote(UUID uuid);
    void addVoteByName(String username);
    boolean hasVoted(UUID uuid);
    boolean hasVotedByName(String username);
    void clearVotes();
    void close();
    void reload();
    void removeVoteByName(String username);
}