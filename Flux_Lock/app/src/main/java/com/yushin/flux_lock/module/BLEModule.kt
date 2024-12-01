package com.yushin.flux_lock.module

import com.yushin.flux_lock.adapter.MultiLayoutRecyclerAdapter
import com.yushin.flux_lock.dispatcher.BLEDispatcher
import com.yushin.flux_lock.model.ViewTypeCell
import com.yushin.flux_lock.store.BLEStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object BLEModule {

    @Provides
    @ActivityScoped
    fun provideStore(bleDispatcher: BLEDispatcher): BLEStore {
        return BLEStore(bleDispatcher)
    }
}