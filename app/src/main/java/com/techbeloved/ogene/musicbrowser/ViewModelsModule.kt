package com.techbeloved.ogene.musicbrowser

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dagger.Binds
import dagger.MapKey
import dagger.Module
import dagger.multibindings.IntoMap
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Provider
import javax.inject.Singleton
import kotlin.reflect.KClass

@Singleton
class ViewModelFactory @Inject constructor(private val providers:
                                           MutableMap<Class<out ViewModel>, Provider<ViewModel>>)
    : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        val provider = providers[modelClass]
                ?: providers.asIterable()
                        .firstOrNull { modelClass.isAssignableFrom(it.key) }
                        ?.value
                ?: throw IllegalArgumentException("Unknown model class: $modelClass")

        try {
            @SuppressWarnings("UNCHECKED_CAST")
            val model = provider.get() as T
            Timber.i("factory: %s, key: %s, provider: %s, model: %s", this, modelClass, provider, model)
            return model
        } catch (e: Exception) {
            Timber.e(e)
            throw RuntimeException(e)
        }
    }
}

@Target(AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER, AnnotationTarget.PROPERTY_SETTER)
@Retention(AnnotationRetention.RUNTIME)
@MapKey
annotation class ViewModelKey(val value: KClass<out ViewModel>)

@Module
interface ViewModelModule {
    @Binds
    fun bindViewModelFactory(factory: ViewModelFactory): ViewModelProvider.Factory

    @Binds
    @IntoMap
    @ViewModelKey(MusicBrowserViewModel::class)
    fun bindMusicBrowserViewModel(viewModel: MusicBrowserViewModel): ViewModel


}
