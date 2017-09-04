package com.acme.petlicense.core

import com.fasterxml.jackson.annotation.JsonProperty
import org.hibernate.annotations.DynamicInsert
import org.hibernate.annotations.DynamicUpdate
import org.hibernate.validator.constraints.NotEmpty
import java.util.Date
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.Table
import javax.persistence.Id
import javax.persistence.Column
import javax.persistence.GeneratedValue
import javax.persistence.GenerationType
import javax.persistence.JoinColumn
import javax.persistence.OneToOne
import javax.validation.constraints.NotNull


@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "licenses")
class License {
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

    @Column(name = "signed_date")
    @JsonProperty
    var signedDate: Date? = null

    @Column(name = "expiration_date")
    @JsonProperty
    var expirationDate: Date? = null

    @OneToOne(cascade = arrayOf(CascadeType.ALL))
    @JoinColumn(name = "assigned_pet_id")
    @JsonProperty
    var assignedPet: Pet? = null
}
