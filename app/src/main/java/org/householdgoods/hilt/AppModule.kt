package org.householdgoods.hilt;

import android.content.Context
import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.android.components.ApplicationComponent;
import dagger.hilt.android.qualifiers.ApplicationContext;
import org.householdgoods.app.Repository

@Module
@InstallIn(ApplicationComponent::class)
object AppModule {
    @Provides
    fun provideRepository(
            @ApplicationContext appContext: Context
    ): Repository {
        return Repository(appContext)
    }
}