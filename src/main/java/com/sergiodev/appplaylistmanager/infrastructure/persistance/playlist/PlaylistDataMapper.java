package com.sergiodev.appplaylistmanager.infrastructure.persistance.playlist;

import com.sergiodev.appplaylistmanager.domain.mapper.DataMapper;
import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import com.sergiodev.appplaylistmanager.infrastructure.persistance.song.SongDataMapper;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {SongDataMapper.class})
public interface PlaylistDataMapper extends DataMapper<Playlist, PlaylistEntity> {
}
