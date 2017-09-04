package com.acme.petlicense

import com.acme.petlicense.core.License
import com.acme.petlicense.core.Pet
import io.dropwizard.client.JerseyClientBuilder
import io.dropwizard.testing.ResourceHelpers
import io.dropwizard.testing.junit.DropwizardAppRule
import org.apache.commons.lang3.RandomStringUtils
import org.apache.commons.lang3.RandomUtils
import org.junit.Assert
import org.junit.BeforeClass
import org.junit.ClassRule
import org.junit.Test
import java.util.*
import javax.ws.rs.client.Client
import javax.ws.rs.client.Entity
import javax.ws.rs.core.GenericType


class PetLicenseApplicationTest {
    companion object {
        @ClassRule
        @JvmField
        val RULE = DropwizardAppRule<PetLicenseConfiguration>(
                PetLicenseService::class.java,
                ResourceHelpers.resourceFilePath("test.yml"))

        lateinit var BASE_URL: String
        lateinit var CLIENT: Client

        @BeforeClass
        @JvmStatic
        fun setup() {
            BASE_URL = "http://localhost:${RULE.localPort}"
            CLIENT = JerseyClientBuilder(RULE.environment).build("test client")
        }
    }

    @Test
    fun add10Licenses() = addLicenses(10, false)

    @Test
    fun add200Licenses() = addLicenses(200, false)

    @Test
    fun add10LicensesWithPets() = addLicenses(10, true)

    @Test
    fun add200LicensesWithPets() = addLicenses(200, true)

    private fun addLicenses(numberOfLicenses: Int, withPets: Boolean) {
        val payload = ArrayList<License>(numberOfLicenses)
        repeat(numberOfLicenses) {
            val license = createRandomLicense(withPets)
            payload.add(license)
        }

        val response = CLIENT.target("$BASE_URL/licenses")
                .request()
                .post(Entity.json(payload))
        Assert.assertEquals(200, response.status)

        val licenses = response.readEntity(object : GenericType<List<License>>() {})

        Assert.assertEquals(numberOfLicenses, licenses.size)
        licenses.forEach { license ->
            license.validate()
            if (withPets) {
                Assert.assertNotNull(license.assignedPet)
                license.assignedPet!!.validate()
            } else {
                Assert.assertNull(license.assignedPet)
            }
        }
    }

    @Test
    fun addInvalidLicenses() {
        val payload = ArrayList<License>(2)
        repeat(2) {
            val license = createRandomLicense()
            license.name = null
            payload.add(license)
        }

        val response = CLIENT.target("$BASE_URL/licenses")
                .request()
                .post(Entity.json(payload))
        Assert.assertEquals(422, response.status)
    }

    @Test
    fun getLicenses() {
        val response = CLIENT.target("$BASE_URL/licenses")
                .request()
                .get()
        Assert.assertEquals(200, response.status)

        val licenses = response.readEntity(object : GenericType<List<License>>() {})

        licenses.assertWithinBounds()
        licenses.forEach { license -> license.validate() }
    }

    @Test
    fun getLicensesWithPets() = getLicensesByAssignedPet(true)

    @Test
    fun getLicensesWithoutPets() = getLicensesByAssignedPet(false)

    private fun getLicensesByAssignedPet(hasAssignedPet: Boolean) {
        val response = CLIENT.target("$BASE_URL/licenses")
                .queryParam("hasAssignedPet", hasAssignedPet)
                .request()
                .get()
        Assert.assertEquals(200, response.status)

        val licenses = response.readEntity(object : GenericType<List<License>>() {})

        licenses.assertWithinBounds()
        licenses.forEach { license ->
            license.validate()
            if (hasAssignedPet) {
                Assert.assertNotNull(license.assignedPet)
                license.assignedPet!!.validate()
            } else {
                Assert.assertNull(license.assignedPet)
            }
        }
    }

    @Test
    fun getLicensesByPetId() {
        var response = CLIENT.target("$BASE_URL/licenses")
                .queryParam("hasAssignedPet", true)
                .request()
                .get()
        Assert.assertEquals(200, response.status)

        var licenses = response.readEntity(object : GenericType<List<License>>() {})

        licenses.assertWithinBounds()

        response.close()
        response = CLIENT.target("$BASE_URL/licenses")
                .queryParam("petId", licenses.first().assignedPet!!.id)
                .request()
                .get()
        Assert.assertEquals(200, response.status)

        licenses = response.readEntity(object : GenericType<List<License>>() {})

        licenses.assertWithinBounds()
        licenses.forEach { license ->
            license.validate()
            Assert.assertNotNull(license.assignedPet)
            license.assignedPet!!.validate()
        }
    }

    @Test
    fun getLicensesWithPagination() {
        val firstBatchLicenses = findLicenses(2,2)
        val secondBatchLicenses = findLicenses(4, 4)

        Assert.assertEquals(4, secondBatchLicenses.size)

        val mergedLicenses = firstBatchLicenses + secondBatchLicenses

        Assert.assertEquals(6, mergedLicenses.size)

        mergedLicenses.forEach { license -> license.validate() }

        var biggestIndex = mergedLicenses.first().id!!
        for (index in 1..5) {
            Assert.assertEquals(--biggestIndex, mergedLicenses.get(index).id!!)
        }
    }

    private fun findLicenses(startIndex: Long, resultCount: Int): List<License> {
        val response = CLIENT.target("$BASE_URL/licenses")
                .queryParam("startIndex", startIndex)
                .queryParam("resultCount", resultCount)
                .request()
                .get()
        Assert.assertEquals(200, response.status)

        val licenses = response.readEntity(object : GenericType<List<License>>() {})

        Assert.assertEquals(resultCount, licenses.size)
        return licenses
    }

    @Test
    fun deleteLicenseById() {
        var response = CLIENT.target("$BASE_URL/licenses")
                .request()
                .get()
        Assert.assertEquals(200, response.status)

        val licenses = response.readEntity(object : GenericType<List<License>>() {})

        licenses.assertWithinBounds()

        response.close()
        response = CLIENT.target("$BASE_URL/licenses/${licenses.first().id}")
                .request()
                .delete()
        Assert.assertEquals(204, response.status)
    }

    @Test
    fun deleteNonExistentLicenseById() {
        val response = CLIENT.target("$BASE_URL/licenses/-1")
                .request()
                .delete()
        Assert.assertEquals(404, response.status)
    }

    @Test
    fun setPetByLicenseId() {
        var response = CLIENT.target("$BASE_URL/licenses")
                .request()
                .get()
        Assert.assertEquals(200, response.status)

        val licenses = response.readEntity(object : GenericType<List<License>>() {})

        licenses.assertWithinBounds()

        response.close()
        response = CLIENT.target("$BASE_URL/licenses/${licenses.first().id}/pet")
                .request()
                .post(Entity.json(createRandomPet()))

        Assert.assertEquals(200, response.status)

        val license = response.readEntity(License::class.java)
        license.validate()
        Assert.assertNotNull(license.assignedPet)
        license.assignedPet!!.validate()
    }

    @Test
    fun setInvalidPetByLicenseId() {
        var response = CLIENT.target("$BASE_URL/licenses")
                .request()
                .get()
        Assert.assertEquals(200, response.status)

        val licenses = response.readEntity(object : GenericType<List<License>>() {})

        licenses.assertWithinBounds()

        response.close()
        val pet = createRandomPet()
        pet.name = null
        response = CLIENT.target("$BASE_URL/licenses/${licenses.first().id}/pet")
                .request()
                .post(Entity.json(pet))

        Assert.assertEquals(422, response.status)
    }

    private fun <T> Collection<T>.assertWithinBounds() {
        Assert.assertTrue(size >= 1)
        Assert.assertTrue(size <= 500)
    }

    private fun License.validate() {
        Assert.assertNotNull(id)
        Assert.assertTrue(id!! > 0L)
        Assert.assertNotNull(name)
        Assert.assertNotEquals("", name)
    }

    private fun Pet.validate() {
        Assert.assertNotNull(id)
        Assert.assertTrue(id!! > 0L)
        Assert.assertNotNull(name)
        Assert.assertNotEquals("", name)
    }

    private fun createRandomLicense(withPets: Boolean = false): License {
        val license = License()
        license.name = RandomStringUtils.randomAlphanumeric(4, 12)
        license.signedDate = createRandomDate(false)
        license.expirationDate = createRandomDate(true)
        if (withPets) {
            license.assignedPet = createRandomPet()
        }
        return license
    }

    private fun createRandomPet(): Pet {
        val pet = Pet()
        pet.name = RandomStringUtils.randomAlphanumeric(4, 12)
        pet.description = RandomStringUtils.randomAlphanumeric(10, 40)
        pet.birthday = createRandomDate(false)
        return pet
    }

    private fun createRandomDate(future: Boolean = false): Date {
        val now = Date().time
        val randomNumber = RandomUtils.nextInt().toLong()
        return if (future) {
            Date(now + randomNumber)
        } else {
            Date(now - randomNumber)
        }
    }
}

