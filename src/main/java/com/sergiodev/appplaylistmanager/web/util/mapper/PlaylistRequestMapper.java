package com.sergiodev.appplaylistmanager.web.util.mapper;

import com.sergiodev.appplaylistmanager.domain.mapper.DataMapper;
import com.sergiodev.appplaylistmanager.domain.model.Playlist;
import com.sergiodev.appplaylistmanager.web.dto.PlaylistRequestDTO;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE,
    unmappedSourcePolicy = ReportingPolicy.IGNORE,
    componentModel = MappingConstants.ComponentModel.SPRING,
    uses = {SongRequestMapper.class})
public interface PlaylistRequestMapper extends DataMapper<Playlist, PlaylistRequestDTO> {
}
