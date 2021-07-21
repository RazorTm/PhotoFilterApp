package com.razortm.imageeditor.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.ScaleGestureDetector
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.MutableLiveData
import com.razortm.imageeditor.adapters.ImageFiltersAdapter
import com.razortm.imageeditor.data.ImageFilter
import com.razortm.imageeditor.databinding.ActivityEditImageBinding
import com.razortm.imageeditor.listeners.ImageFilterListener
import com.razortm.imageeditor.utilities.displayToast
import com.razortm.imageeditor.utilities.show
import com.razortm.imageeditor.viewmodels.EditImageViewModel
import jp.co.cyberagent.android.gpuimage.GPUImage
import org.koin.androidx.viewmodel.ext.android.viewModel


class EditImageActivity : AppCompatActivity(), ImageFilterListener {

    companion object {
        const val KEY_FILTERED_IMAGE_URI = "filteredImageUri"
    }

    private lateinit var binding: ActivityEditImageBinding
    private val viewModel: EditImageViewModel by viewModel()
    private lateinit var gpuImage: GPUImage

    //Image Bitmaps
    private lateinit var originalBitmap: Bitmap
    private val filteredBitmap = MutableLiveData<Bitmap>()

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setListeners()
        setupObservers()
        prepareImagePreview()

        var scaleFactor = 1f
        val scaleGestureDetector = ScaleGestureDetector(
            this,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
                override fun onScale(detector: ScaleGestureDetector): Boolean {
                    scaleFactor *= detector.scaleFactor
                    scaleFactor = scaleFactor.coerceIn(0.1f, 5.0f)

                    binding.imagePreview.scaleX = scaleFactor
                    binding.imagePreview.scaleY = scaleFactor
                    //imageView.invalidate()

                    return super.onScale(detector)
                }
            }
        )

        binding.imagePreview.setOnTouchListener { _, event ->
            scaleGestureDetector.onTouchEvent(event)
        }
    }


    private fun setupObservers() {
        viewModel.imagePreviewUiState.observe(this, {
            val dataState = it ?: return@observe
            binding.previewProgressBar.visibility =
                if(dataState.isLoading) View.VISIBLE else View.GONE
            dataState.bitmap?.let { bitmap ->
                //For the first time filtered image = original image
                originalBitmap = bitmap
                filteredBitmap.value = bitmap

                with(originalBitmap){
                    gpuImage.setImage(this)
                    binding.imagePreview.show()
                    viewModel.loadImageFilters(this)
                }
            } ?: kotlin.run {
                dataState.error?.let { error ->
                    displayToast(error)
                }
            }
        })
        viewModel.imageFiltersUIState.observe(this, {
            val imageFiltersDataState = it ?: return@observe
            binding.imageFiltersProgressBar.visibility =
                if(imageFiltersDataState.isLoading) View.VISIBLE else View.GONE
            imageFiltersDataState.imageFilters?.let { imageFilters ->
                ImageFiltersAdapter(imageFilters, this).also {adapter ->
                    binding.filtersRecyclerView.adapter = adapter
                }
            } ?: kotlin.run {
                imageFiltersDataState.error?.let { error->
                    displayToast(error)
                }
            }
        })
        filteredBitmap.observe(this, { bitmap->
            binding.imagePreview.setImageBitmap(bitmap)
        })
        viewModel.saveFilteredImageUIState.observe(this, {
            val saveFilteredImageDataState = it ?: return@observe
            if(saveFilteredImageDataState.isLoading) {
                binding.imageSave.visibility = View.GONE
                binding.savingProgressBar.visibility = View.VISIBLE
            }else{
                binding.savingProgressBar.visibility = View.GONE
                binding.imageSave.visibility = View.VISIBLE
            }
            saveFilteredImageDataState.uri?.let { savedImageUri->
                Intent(
                    applicationContext,
                    FilteredImageActivity::class.java
                ).also { filteredImageIntent ->
                    filteredImageIntent.putExtra(KEY_FILTERED_IMAGE_URI, savedImageUri)
                    startActivity(filteredImageIntent)
                }
            } ?: kotlin.run {
                saveFilteredImageDataState.error?.let {error->
                    displayToast(error)
                }
            }
        })
    }

    private fun prepareImagePreview() {
        gpuImage = GPUImage(applicationContext)
        intent.getParcelableExtra<Uri>(MainActivity.KEY_IMAGE_URI)?.let { imageUri->
            viewModel.prepareImagePreview(imageUri)
    }}

    private fun setListeners() {
        binding.imageBack.setOnClickListener {
            onBackPressed()
        }

        binding.imageSave.setOnClickListener {
            filteredBitmap.value?.let { bitmap ->  
                viewModel.saveFilteredImage(bitmap)
            }
        }

        /*
        This will show original image when we long click the ImageView until we released click,
        So that we can see difference between original and filtered
         */
//        binding.imagePreview.setOnLongClickListener {
//            binding.imagePreview.setImageBitmap(originalBitmap)
//            return@setOnLongClickListener false
//        }
//        binding.imagePreview.setOnClickListener {
//            binding.imagePreview.setImageBitmap(filteredBitmap.value)
//        }
    }

    override fun onFilterSelected(imageFilter: ImageFilter) {
        with(imageFilter){
            with(gpuImage){
                setFilter(filter)
                filteredBitmap.value = bitmapWithFilterApplied
            }
        }
    }
}