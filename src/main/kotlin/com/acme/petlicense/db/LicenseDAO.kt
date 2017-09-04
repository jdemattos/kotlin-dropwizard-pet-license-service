package com.acme.petlicense.db

import com.acme.petlicense.core.License
import org.hibernate.SessionFactory
import org.hibernate.criterion.Order
import org.hibernate.criterion.Restrictions
import java.util.*


class LicenseDAO(factory: SessionFactory) : BaseDAO<License>(factory) {
    fun find(
            startIndex: Int,
            resultCount: Int,
            hasAssignedPet: Optional<Boolean>,
            assignedPetId: Optional<Long>): MutableList<License> {
        val queryCriteria = criteria()
                .setFirstResult(startIndex)
                .setMaxResults(resultCount)
                .addOrder(Order.desc("id"))
        hasAssignedPet.ifPresent {
            if (it) {
                queryCriteria.add(Restrictions.isNotNull("assignedPet"))
            } else {
                queryCriteria.add(Restrictions.isNull("assignedPet"))
            }
        }
        assignedPetId.ifPresent {
            queryCriteria
                    .createAlias("assignedPet", "pet")
                    .add(Restrictions.eq("pet.id", it))
        }

        return list(queryCriteria)
    }
}