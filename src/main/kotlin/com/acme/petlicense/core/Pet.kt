package com.acme.petlicense.core

import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.validator.constraints.NotEmpty
import java.util.Date

import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.validation.constraints.NotNull


@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "pets")
class Pet {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty
    var id: Long? = null

    @Column(name = "name", nullable = false)
    @NotNull
    @NotEmpty
    @JsonProperty
    var name: String? = null

    @Column(name = "description", nullable = false)
    @JsonProperty
    var description: String? = null

    @Column(name = "birthday")
    @JsonProperty
    var birthday: Date? = null
}
