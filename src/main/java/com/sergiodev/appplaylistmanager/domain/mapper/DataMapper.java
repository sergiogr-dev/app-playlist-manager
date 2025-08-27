package com.sergiodev.appplaylistmanager.domain.mapper;

import java.util.List;

/**
 *
 * @param <D> Domain model
 * @param <I> Infrastructure object
 */
public interface DataMapper<D, I> {
    D toDomain(I model);
    I toObject(D entity);
    List<D> toDomainList(List<I> models);
    List<I> toObjectList(List<D> entities);
}
