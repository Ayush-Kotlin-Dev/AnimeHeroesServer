package com.example.di

import com.example.Repository.HeroRepository
import com.example.Repository.HeroRepositoryImpl
import org.koin.dsl.module

val koinModule = module {
    single<HeroRepository> {
        HeroRepositoryImpl()
    }
}