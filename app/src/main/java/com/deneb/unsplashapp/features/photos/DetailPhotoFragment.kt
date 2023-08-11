package com.deneb.unsplashapp.features.photos

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import com.deneb.unsplashapp.core.exception.Failure
import com.deneb.unsplashapp.core.extensions.failure
import com.deneb.unsplashapp.core.extensions.loadFromUrl
import com.deneb.unsplashapp.core.extensions.observe
import com.deneb.unsplashapp.core.platform.BaseFragment
import com.deneb.unsplashapp.databinding.FragmentDetailPhotoBinding
import com.deneb.unsplashapp.features.photos.model.UnsplashDetailView
import com.deneb.unsplashapp.features.photos.model.UnsplashItemView
import dagger.hilt.android.AndroidEntryPoint
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs

@AndroidEntryPoint
class DetailPhotoFragment :
    BaseFragment<FragmentDetailPhotoBinding>(FragmentDetailPhotoBinding::inflate) {
    private val photoDetailsViewModel by viewModels<PhotosDetailViewModel>()
    //Aquí obtengo los argumentos pasados por safeargs
    private val args: DetailPhotoFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        with(photoDetailsViewModel) {
            observe(photoDetails, ::renderPhotoDetail)
            failure(failure, ::renderFailure)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loadPhotoDetail()
    }

    private fun loadPhotoDetail() {
        showProgress()
        //aquí uso el id del argumento pasado por safeargs para pedir el detalle de la foto
        photoDetailsViewModel.loadPhotoDetails(
            args.photo.id
        )
    }

    private fun renderPhotoDetail(unsplashDetailView: UnsplashDetailView?) {
        bindUser(unsplashDetailView)
        bindCamera(unsplashDetailView)

        hideProgress()
    }

    private fun bindCamera(unsplashDetailView: UnsplashDetailView?) {
        with(binding) {
            cameraModel.text = unsplashDetailView?.exif?.model
            cameraIso.text = "ISO: ${unsplashDetailView?.exif?.iso}"
            cameraAperture.text = "Aperture: ${unsplashDetailView?.exif?.aperture}"
        }
    }

    private fun bindUser(unsplashDetailView: UnsplashDetailView?) {
        with(binding) {
            imageDetail.loadFromUrl(unsplashDetailView?.image)
            circleImageProfile.loadFromUrl(unsplashDetailView?.user?.profileImage?.small)
            profileName.text =
                "${unsplashDetailView?.user?.firstName} ${unsplashDetailView?.user?.lastName}"
            instagramName.text = unsplashDetailView?.user?.instagramUsername
        }
    }

    private fun renderFailure(failure: Failure?) {
        when (failure) {
            is Failure.NetworkConnection -> Log.e(DetailPhotoFragment::class.java.canonicalName, "Network Connection Error")
            is Failure.ServerError -> Log.e(DetailPhotoFragment::class.java.canonicalName, "Server Error")
            else -> Log.e(DetailPhotoFragment::class.java.canonicalName, "Error")
        }
    }

}