package com.acme.petlicense.resources

import com.acme.petlicense.core.License
import com.acme.petlicense.core.Pet
import com.acme.petlicense.db.LicenseDAO
import io.dropwizard.hibernate.UnitOfWork
import javax.validation.Valid
import javax.ws.rs.Consumes
import javax.ws.rs.DELETE
import javax.ws.rs.NotFoundException
import javax.ws.rs.POST
import javax.ws.rs.Path
import javax.ws.rs.PathParam
import javax.ws.rs.Produces
import javax.ws.rs.core.MediaType


@Path("/licenses/{licenseId}")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
class LicenseResource(private val licenseDao: LicenseDAO) {
    @DELETE
    @UnitOfWork
    fun deleteLicenseById(@PathParam("licenseId") licenseId: Long) {
        val license = findSafely(licenseId)
        licenseDao.delete(license)
    }

    @POST
    @Path("/pet")
    @UnitOfWork
    @Valid
    fun updatePetByLicenseId(
            @PathParam("licenseId") licenseId: Long,
            @Valid pet: Pet): License {
        val license = findSafely(licenseId)
        license.assignedPet = pet
        return licenseDao.save(license)
    }

    private fun findSafely(licenseId: Long): License {
        return licenseDao.findById(licenseId).orElseThrow({ NotFoundException("No license found with id=${licenseId}.") })
    }
}
