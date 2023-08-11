package com.deneb.unsplashapp.features.photos

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.deneb.unsplashapp.core.interactor.UseCase
import com.deneb.unsplashapp.core.platform.BaseViewModel
import com.deneb.unsplashapp.features.photos.model.UnsplashItemView
import com.deneb.unsplashapp.features.photos.model.UnsplashResponseItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class PhotosViewModel
@Inject constructor(private val getPhotos: GetPhotos) : BaseViewModel() {

    private val _photos: MutableLiveData<List<UnsplashItemView>> = MutableLiveData()
    val photos: LiveData<List<UnsplashItemView>> = _photos


    fun loadPhotos(page: Int) {
        getPhotos(page, viewModelScope) { it.fold(::handleFailure, ::handlePhotoList) }

    }

    private fun handlePhotoList(photos: List<UnsplashItemView>) {
        //Aquí he realizado la lógica de paginación
        //newPhotos es la variable que va a guardar las fotos que nos vengan de la respuesta de la última llamada que hayamos hecho
        val newPhotos = photos
        //currentPhotos cogerá el valor de la MutableLiveData, si es la primera vez será empty, despoués siempre tendrá datos.
        val currentPhotos = _photos.value.orEmpty().toMutableList()
        //Aquí añadimos a currentPhotos todas las fotos que acabamos de obtener desde la llamada
        currentPhotos.addAll(newPhotos)
        //Cambiamos el valor de la mutableLiveData para que el observer lo detecte y nos cambie la vista.
        _photos.value = currentPhotos
    }
}