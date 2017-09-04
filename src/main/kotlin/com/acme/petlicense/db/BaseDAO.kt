package com.acme.petlicense.db

import io.dropwizard.hibernate.AbstractDAO
import org.hibernate.Session
import org.hibernate.SessionFactory
import org.hibernate.criterion.Order
import java.util.*


open class BaseDAO<Entity>(factory: SessionFactory) : AbstractDAO<Entity>(factory) {
    fun find(startIndex: Int, resultCount: Int): MutableList<Entity> {
        return list(criteria()
                .setFirstResult(startIndex)
                .setMaxResults(resultCount)
                .addOrder(Order.desc("id")))
    }

    fun findById(id: Long): Optional<Entity> {
        return Optional.ofNullable(get(id))
    }

    fun delete(entity: Entity) {
        currentSession().delete(entity)
    }

    fun save(entity: Entity): Entity {
        return merge(entity)
    }

    fun save(entities: List<Entity>): List<Entity> {
        val session = currentSession()
        val results = entities.mapIndexed { index, entity ->
            val result = merge(entity, session)
            if (index % 25 == 0) {
                session.flush()
                session.clear()
            }
            return@mapIndexed result
        }
        return results
    }

    private fun merge(entity: Entity, session: Session = currentSession()): Entity {
        return session.merge(entity) as Entity
    }
}