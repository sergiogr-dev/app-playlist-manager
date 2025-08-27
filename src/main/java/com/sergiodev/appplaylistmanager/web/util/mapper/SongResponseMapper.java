package com.sergiodev.appplaylistmanager.web.util.mapper;

import com.sergiodev.appplaylistmanager.domain.mapper.DataMapper;
import com.sergiodev.appplaylistmanager.domain.model.Song;
import com.sergiodev.appplaylistmanager.web.dto.SongResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING)
public interface SongResponseMapper extends DataMapper<Song, SongResponseDTO> {
}
