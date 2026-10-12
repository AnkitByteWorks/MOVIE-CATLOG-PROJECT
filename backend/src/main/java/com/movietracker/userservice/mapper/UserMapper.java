package com.movietracker.userservice.mapper;

import com.movietracker.userservice.dto.response.PublicUserResponse;
import com.movietracker.userservice.dto.response.UserProfileResponse;
import com.movietracker.userservice.dto.response.UserResponse;
import com.movietracker.userservice.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * MapStruct mapper for transforming {@link User} domain entities to API response DTOs.
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Converts a {@link User} persistence entity to a safe {@link UserResponse} DTO.
     *
     * @param user source entity
     * @return safe public user profile response
     */
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    UserResponse toResponse(User user);

    /**
     * Converts a {@link User} entity to a public profile response.
     *
     * @param user source entity
     * @return public user profile response
     */
    PublicUserResponse toPublicResponse(User user);

    /**
     * Converts a {@link User} entity to a full owner profile response.
     *
     * @param user source entity
     * @return authenticated owner profile response
     */
    @Mapping(target = "role", expression = "java(user.getRole() != null ? user.getRole().name() : null)")
    UserProfileResponse toProfileResponse(User user);
}
