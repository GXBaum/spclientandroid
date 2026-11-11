package de.rafaelbeckmann.hvkclient

import org.koin.core.annotation.ComponentScan
import org.koin.core.annotation.Module

@Module
@ComponentScan("de.rafaelbeckmann.hvkclient.workers")
class AndroidWorkerModule