package com.deneb.unsplashapp.features.photos

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.deneb.unsplashapp.core.exception.Failure
import com.deneb.unsplashapp.core.extensions.failure
import com.deneb.unsplashapp.core.extensions.observe
import com.deneb.unsplashapp.core.platform.BaseFragment
import com.deneb.unsplashapp.databinding.FragmentPhotosBinding
import com.deneb.unsplashapp.features.photos.model.UnsplashItemView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PhotosFragment : BaseFragment<FragmentPhotosBinding>(FragmentPhotosBinding::inflate) {

    @Inject
    lateinit var photosAdapter: PhotosAdapter
    private val photosViewModel: PhotosViewModel by viewModels()
    //Creamos una variable para controlar la paginación y le vamos sumando uno cada vez que hacemos scroll y llegamos al último ítem del recycler
    private var pageToLoad = 1
    //Creamos un flag isLoading para controlar que no se hagan llamadas simultáneas a la hora de llegar al final del recycler (Esto hubiera podido ser una LiveData y haberse controlado con un observer)
    private var isLoading = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(photosViewModel) {
            observe(photos, ::renderPhotoList)
            failure(failure, ::renderFailure)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeView()
        loadPhotoList()
    }

    private fun initializeView() {
        //He añadido el whith(binding) para que el propio binding esté implícito y no tener que repetir binding.loquesea todo el rato
        with(binding) {

            photoList.layoutManager = StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
            photoList.adapter = photosAdapter

            //He añadido un scrolllistener para detectar cuando el usuario ha llegado al final del recycler para lanzar la llamada otra vez
            photoList.addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    //he creado una variable y he metodop dentro el layoutmanager para poder acceder fácilente al mismo más abajo
                    val layoutManager = recyclerView.layoutManager as StaggeredGridLayoutManager
                    //Aquí obtenemos los el total de los items visibles del recycler
                    val visibleItemCount = layoutManager.childCount
                    //Aquí obtenemos la cuenta total de items del recycler sin importar su visibilidad
                    val totalItemCount = layoutManager.itemCount
                    //Aqui tenemos la primera posición visible del recycler
                    val firstVisibleItemPositions = layoutManager.findFirstVisibleItemPositions(null)
                    //y aquí la última posición visible del recycler
                    val lastVisibleItemPosition = firstVisibleItemPositions.maxOrNull() ?: 0

                    // Aquí verificamos si el último elemento visible más la cantidad de elementos visibles total es igual o nmayor que el número total de elementos, si devuelve true hemos llegado al final
                    if (visibleItemCount + lastVisibleItemPosition >= totalItemCount) {
                        //Aquí controlamos la llamada múltiple al endpoint
                        if (!isLoading) {
                            //Al poner este valor a true no vamos a dejar que se vuelva a hacer este bloque sin poner el flag a false
                            isLoading = true
                            //Sumamos uno a la página a cargar para no cargar la misma todo el rato.
                            pageToLoad++
                            //Llamamos a la función que hace la llamada y la carga de los datos
                            loadPhotoList()
                        }
                    }
                }
            })


            photosAdapter.clickListener = { photo ->
                //Aquí he hecho la nueva navegación con SafeArgs he modificado el código en main_navigation.
                val action = PhotosFragmentDirections.actionOpenPhotoDetail(photo)
                findNavController().navigate(action)
            }
        }
    }

    private fun loadPhotoList() {
        showProgress()
        photosViewModel.loadPhotos(pageToLoad)
    }

    private fun renderPhotoList(photos: List<UnsplashItemView>?) {
        photosAdapter.collection = photos.orEmpty()
        hideProgress()
        //Aquí cambiamos el valor del flag de carga de datos para que podamos seguir llamando y paginando la respuesta.
        isLoading = false
    }

    private fun renderFailure(failure: Failure?) {
        when (failure) {
            is Failure.NetworkConnection -> Log.e(DetailPhotoFragment::class.java.canonicalName, "Network Connection Error")
            is Failure.ServerError -> Log.e(DetailPhotoFragment::class.java.canonicalName, "Server Error")
            else -> Log.e(DetailPhotoFragment::class.java.canonicalName, "Error")
        }
    }

}