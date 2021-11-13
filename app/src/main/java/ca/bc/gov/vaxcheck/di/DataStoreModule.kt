package ca.bc.gov.vaxcheck.di

import android.content.Context
import ca.bc.gov.shcdecoder.SHCVerifier
import ca.bc.gov.shcdecoder.SHCConfig
import ca.bc.gov.shcdecoder.SHCVerifierImpl
import ca.bc.gov.vaxcheck.R
import ca.bc.gov.vaxcheck.data.local.DataStoreRepo
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [DataStoreModule]
 *
 * @author amit metri
 */
@Module
@InstallIn(SingletonComponent::class)
class DataStoreModule {

    @Provides
    @Singleton
    fun providesDataStore(@ApplicationContext context: Context) = DataStoreRepo(context)
}
