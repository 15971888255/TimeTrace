package com.example.timetrace.di

import com.example.timetrace.data.repository.MainRepository
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface WidgetRepoEntryPoint {
    fun repository(): MainRepository
}
