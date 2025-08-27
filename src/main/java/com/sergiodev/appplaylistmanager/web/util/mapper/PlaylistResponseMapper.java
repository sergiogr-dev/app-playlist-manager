package com.sergiodev.appplaylistmanager.web.util.mapper;

import com.sergiodev.appplaylistmanager.domain.mapper.DataMapper;
import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import com.sergiodev.appplaylistmanager.web.dto.PlaylistResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {SongResponseMapper.class})
public interface PlaylistResponseMapper extends DataMapper<Playlist, PlaylistResponseDTO> {
}
