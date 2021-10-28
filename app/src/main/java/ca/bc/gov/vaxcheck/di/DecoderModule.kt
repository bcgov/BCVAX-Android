package ca.bc.gov.vaxcheck.di

import android.content.Context
import ca.bc.gov.shcdecoder.BcCardVerifier
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * [DecoderModule]
 *
 *
 * @author Pinakin Kansara
 */
@Module
@InstallIn(SingletonComponent::class)
class DecoderModule {

    @Provides
    @Singleton
    fun provideBCVerifier(@ApplicationContext context: Context) = BcCardVerifier(context)
}
