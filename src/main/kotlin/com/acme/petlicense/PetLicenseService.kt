package com.acme.petlicense

import com.acme.petlicense.core.License
import com.acme.petlicense.core.Pet
import com.acme.petlicense.db.LicenseDAO
import com.acme.petlicense.resources.LicenseResource
import com.acme.petlicense.resources.LicensesResource
import com.fasterxml.jackson.annotation.JsonInclude
import io.dropwizard.Application
import io.dropwizard.db.DataSourceFactory
import io.dropwizard.setup.Bootstrap
import io.dropwizard.setup.Environment
import io.dropwizard.hibernate.HibernateBundle
import io.dropwizard.migrations.MigrationsBundle


class PetLicenseService : Application<PetLicenseConfiguration>() {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            PetLicenseService().run(*args)
        }
    }

    override fun initialize(bootstrap: Bootstrap<PetLicenseConfiguration>) {
        super.initialize(bootstrap)
        bootstrap.addBundle(migrations)
        bootstrap.addBundle(hibernate)
    }

    override fun run(config: PetLicenseConfiguration, environment: Environment) {
        environment.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL)
        environment.jersey().register(LicenseResource(LicenseDAO(hibernate.sessionFactory)))
        environment.jersey().register(LicensesResource(LicenseDAO(hibernate.sessionFactory)))
    }

    private val migrations = object : MigrationsBundle<PetLicenseConfiguration>() {
        override fun getDataSourceFactory(configuration: PetLicenseConfiguration): DataSourceFactory {
            return configuration.dataSourceFactory
        }
    }

    private val hibernate = object : HibernateBundle<PetLicenseConfiguration>(
            License::class.java,
            Pet::class.java) {
        override fun getDataSourceFactory(configuration: PetLicenseConfiguration): DataSourceFactory {
            return configuration.dataSourceFactory
        }
    }
}