package com.studenttracker.dao;

import com.studenttracker.model.MissionDraft;
import java.util.List;

public interface MissionDraftDAO {
    Integer insert(MissionDraft draft);
    boolean update(MissionDraft draft);
    boolean delete(int draftId);
    MissionDraft findById(int draftId);
    List<MissionDraft> findAll();
    MissionDraft findByMissionId(int missionId);
    boolean upsert(MissionDraft draft);
    boolean deleteByMissionId(int missionId);
}