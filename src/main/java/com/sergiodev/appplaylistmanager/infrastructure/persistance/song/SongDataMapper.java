package com.sergiodev.appplaylistmanager.infrastructure.persistance.song;

import com.sergiodev.appplaylistmanager.domain.mapper.DataMapper;
import com.sergiodev.appplaylistmanager.domain.model.Song;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING)
public interface SongDataMapper extends DataMapper<Song, SongEntity> {
}
