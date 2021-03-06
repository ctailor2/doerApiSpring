package com.doerapispring.domain

trait OwnedObjectWriteRepository[T, OwnerId, Id] {
    def save(ownerId: OwnerId, id: Id, model: T): Unit = {
        saveAll(ownerId, id, List(model))
    }

    def saveAll(ownerId: OwnerId, id: Id, models: List[T]): Unit
}
