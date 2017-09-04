package com.acme.petlicense.resources

import com.acme.petlicense.core.License
import com.acme.petlicense.db.LicenseDAO
import io.dropwizard.hibernate.UnitOfWork
import java.util.*
import javax.validation.Valid
import javax.validation.constraints.Max
import javax.validation.constraints.Min
import javax.ws.rs.Consumes
import javax.ws.rs.DefaultValue
import javax.ws.rs.GET
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.Produces
import javax.ws.rs.QueryParam
import javax.ws.rs.core.MediaType


@Path("/licenses")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class LicensesResource(private val licenseDao: LicenseDAO) {
    @GET
    @UnitOfWork
    fun find(
            @DefaultValue("0") @Min(0) @QueryParam("startIndex") startIndex: Int,
            @DefaultValue("500") @Min(1) @Max(500) @QueryParam("resultCount") resultCount: Int,
            @QueryParam("hasAssignedPet") hasAssignedPet: Optional<Boolean>,
            @QueryParam("petId") petId: Optional<Long>): List<License> {

        return licenseDao.find(
                startIndex,
                resultCount,
                hasAssignedPet,
                petId)
    }

    @POST
    @UnitOfWork
    fun addMany(@Valid licenses: List<License>): List<License> {
        return licenseDao.save(licenses)
    }
}
