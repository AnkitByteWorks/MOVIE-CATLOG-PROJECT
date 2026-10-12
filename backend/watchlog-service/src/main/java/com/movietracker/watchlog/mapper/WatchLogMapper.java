package com.movietracker.watchlog.mapper;

import com.movietracker.watchlog.dto.response.PublicUserWatchLogResponse;
import com.movietracker.watchlog.dto.response.WatchLogResponse;
import com.movietracker.watchlog.entity.WatchLog;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

/**
 * MapStruct mapper converting between {@link WatchLog} entity and API DTO representations.
 */
@Mapper(componentModel = "spring")
public interface WatchLogMapper {

    @Mapping(target = "posterUrl", source = "posterPath", qualifiedByName = "toPosterUrl")
    @Mapping(target = "backdropUrl", source = "backdropPath", qualifiedByName = "toBackdropUrl")
    WatchLogResponse toResponse(WatchLog entity);

    @Mapping(target = "posterUrl", source = "posterPath", qualifiedByName = "toPosterUrl")
    @Mapping(target = "backdropUrl", source = "backdropPath", qualifiedByName = "toBackdropUrl")
    PublicUserWatchLogResponse toPublicResponse(WatchLog entity);

    @Named("toPosterUrl")
    default String toPosterUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        return "https://image.tmdb.org/t/p/w500" + (path.startsWith("/") ? path : "/" + path);
    }

    @Named("toBackdropUrl")
    default String toBackdropUrl(String path) {
        if (path == null || path.isBlank()) {
            return null;
        }
        if (path.startsWith("http://") || path.startsWith("https://")) {
            return path;
        }
        return "https://image.tmdb.org/t/p/w1280" + (path.startsWith("/") ? path : "/" + path);
    }
}
